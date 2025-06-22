package com.example.smartee.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
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

    private val serverSocket: BluetoothServerSocket? = try {
        when {
            bluetoothAdapter == null -> {
                Log.e("BluetoothServer", "❌ BluetoothAdapter is null")
                null
            }
            !bluetoothAdapter.isEnabled -> {
                Log.e("BluetoothServer", "❌ Bluetooth is turned off")
                null
            }
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED -> {
                Log.w("BluetoothServer", "❌ BLUETOOTH_CONNECT permission not granted")
                null
            }
            else -> {
                bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "AttendanceServer",
                    UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
                )
            }
        }
    } catch (e: SecurityException) {
        Log.e("BluetoothServer", "❌ SecurityException during socket creation", e)
        null
    } catch (e: IOException) {
        Log.e("BluetoothServer", "❌ IOException during socket creation", e)
        null
    }

    override fun run() {
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
            val userId = json.getString("userId")
            val code = json.getInt("code")

            GlobalScope.launch(Dispatchers.IO) {
                processAttendance(studyId, userId, code)
            }

        } catch (e: Exception) {
            Log.e("BluetoothServer", "❌ Error handling client socket", e)
        }
    }

    private suspend fun processAttendance(studyId: String, userId: String, code: Int) {
        val db = FirebaseFirestore.getInstance()
        val sessionRef = db.collection("attendanceSessions").document(studyId)

        val sessionSnap = sessionRef.get().await()
        val validCode = sessionSnap.getLong("code")?.toInt()

        if (validCode == code) {
            val memberRef = db.collection("studies")
                .document(studyId)
                .collection("members")
                .document(userId)

            db.runTransaction { transaction ->
                val snap = transaction.get(memberRef)
                val current = (snap.getLong("currentCount") ?: 0).toInt()
                val total = (snap.getLong("totalCount") ?: 0).toInt()

                transaction.update(memberRef, mapOf(
                    "isPresent" to true,
                    "currentCount" to current + 1,
                    "totalCount" to total + 1
                ))
            }.addOnSuccessListener {
                Log.d("BluetoothServer", "✅ 출석 처리 완료 ($userId)")
            }.addOnFailureListener {
                Log.e("BluetoothServer", "❌ 트랜잭션 실패", it)
            }
        } else {
            Log.w("BluetoothServer", "❌ 코드 불일치 ($userId)")
        }
    }
}
