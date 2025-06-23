// BluetoothServerService.kt
package com.example.smartee.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.smartee.model.UserData
import com.example.smartee.repository.StudyRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.util.*

class BluetoothServerService(private val context: Context) : Thread() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // [추가] Repository 인스턴스 생성
    private val studyRepository = StudyRepository()
    private val db = FirebaseFirestore.getInstance()

    private val serverSocket: BluetoothServerSocket? = try {
        // ... (소켓 생성 로직은 이전과 동일)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { null }
        else { bluetoothAdapter?.listenUsingRfcommWithServiceRecord("AttendanceServer", UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")) }
    } catch (e: Exception) {
        Log.e("BluetoothServer", "❌ Socket creation failed", e)
        null
    }

    override fun run() {
        // ... (run 메소드 로직은 이전과 동일)
        if (serverSocket == null) {
            Log.w("BluetoothServer", "⚠️ Server socket not available, Bluetooth not started")
            return
        }
        while (true) {
            try {
                val socket = serverSocket.accept()
                socket?.let {
                    handleClientSocket(it)
                    it.close()
                }
            } catch (e: IOException) {
                Log.e("BluetoothServer", "❌ Socket accept failed", e)
                break
            }
        }
    }

    private fun handleClientSocket(socket: BluetoothSocket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            val message = reader.readLine()

            Log.d("BluetoothServer", "📩 Received: $message")

            val json = JSONObject(message)
            val studyId = json.getString("studyId")
            val meetingId = json.getString("meetingId") // [추가] meetingId 파싱
            val userId = json.getString("userId")

            GlobalScope.launch(Dispatchers.IO) {
                // [수정] 자체적인 DB 처리 로직 대신 Repository의 markAttendance 함수를 호출
                processAttendanceWithRepository(studyId, meetingId, userId)
            }

        } catch (e: Exception) {
            Log.e("BluetoothServer", "❌ Error handling client socket", e)
        }
    }

    // [수정] Repository를 사용하도록 함수 이름 및 로직 변경
    private suspend fun processAttendanceWithRepository(studyId: String, meetingId: String, userId: String) {
        try {
            // 출석 기록용 문서를 생성할 때 필요한 사용자 닉네임을 가져옵니다.
            val userDoc = db.collection("users").document(userId).get().await()
            val userName = userDoc.toObject(UserData::class.java)?.nickname ?: "알수없음"
            val studyDoc = db.collection("studies").document(studyId).get().await()
            val studyName = studyDoc.getString("title") ?: ""

            // Repository의 안정적인 출석 처리 함수 호출
            studyRepository.markAttendance(
                meetingId = meetingId,
                userId = userId,
                parentStudyId = studyId,
                userName = userName,
                studyName = studyName
            ).await()
            Log.d("BluetoothServer", "✅ 출석 처리 완료 (Repository) - User: $userId, Meeting: $meetingId")
        } catch (e: Exception) {
            Log.e("BluetoothServer", "❌ Repository 출석 처리 실패", e)
        }
    }
}