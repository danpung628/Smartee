// smartee/bluetooth/BluetoothServerService.kt

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
    private val studyRepository = StudyRepository()
    private val db = FirebaseFirestore.getInstance()

    private val serverSocket: BluetoothServerSocket? = try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { null }
        else { bluetoothAdapter?.listenUsingRfcommWithServiceRecord("AttendanceServer", UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")) }
    } catch (e: Exception) {
        Log.e("BluetoothServer", "❌ Socket creation failed", e)
        null
    }

    override fun run() {
        if (serverSocket == null) return

        while (true) {
            try {
                serverSocket.accept()?.let {
                    handleClientSocket(it)
                    it.close()
                }
            } catch (e: IOException) {
                break
            }
        }
    }

    private fun handleClientSocket(socket: BluetoothSocket) {
        try {
            val message = BufferedReader(InputStreamReader(socket.inputStream)).readLine()
            Log.d("BluetoothServer", "📩 Received: $message")

            val json = JSONObject(message)
            val studyId = json.getString("studyId")
            val meetingId = json.getString("meetingId")
            val userId = json.getString("userId")

            GlobalScope.launch(Dispatchers.IO) {
                processAttendanceWithRepository(studyId, meetingId, userId)
            }
        } catch (e: Exception) {
            Log.e("BluetoothServer", "❌ Error handling client socket", e)
        }
    }

    // [수정] Repository의 올바른 출석 처리 함수를 호출하는 로직
    private suspend fun processAttendanceWithRepository(studyId: String, meetingId: String, userId: String) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val userName = userDoc.toObject(UserData::class.java)?.nickname ?: "알수없음"
            val studyDoc = db.collection("studies").document(studyId).get().await()
            val studyName = studyDoc.getString("title") ?: ""

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