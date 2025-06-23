// smartee/bluetooth/BluetoothServerService.kt

package com.example.smartee.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
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

class BluetoothServerService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val studyRepository = StudyRepository()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val TAG = "BluetoothServerService"
    }

    private val advertiser by lazy { bluetoothAdapter?.bluetoothLeAdvertiser }
    private var serverSocket: BluetoothServerSocket? = null
    private var serverThread: Thread? = null

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "✅ BLE Advertising started successfully.")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "❌ BLE Advertising onStartFailure: $errorCode")
        }
    }

    fun start(meetingId: String) {
        startAdvertising(meetingId)
        startServerThread()
    }

    fun stop() {
        stopAdvertising()
        stopServerThread()
    }

    private fun startAdvertising(meetingId: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "❌ BLUETOOTH_ADVERTISE permission not granted")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false) // 이름 대신 서비스 UUID로 식별
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(SERVICE_UUID), meetingId.toByteArray(Charsets.UTF_8))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private fun stopAdvertising() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        advertiser?.stopAdvertising(advertiseCallback)
        Log.d(TAG, "✅ BLE Advertising stopped.")
    }

    private fun startServerThread() {
        if (serverThread?.isAlive == true) return // 이미 실행 중이면 다시 시작하지 않음

        serverThread = Thread {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "❌ BLUETOOTH_CONNECT permission not granted")
                return@Thread // [수정] return@thread -> return@Thread
            }

            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("SmarteeAttendance", SERVICE_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "❌ Socket listen() failed", e)
                return@Thread
            }

            while (!Thread.currentThread().isInterrupted) {
                try {
                    serverSocket?.accept()?.let { socket ->
                        Log.d(TAG, "🤝 RFCOMM connection accepted.")
                        handleClientSocket(socket)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "❌ Socket accept() failed", e)
                    break
                }
            }
        }
        serverThread?.start()
    }

    private fun stopServerThread() {
        serverThread?.interrupt()
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "❌ Error closing server socket", e)
        }
        serverThread = null
        Log.d(TAG, "✅ RFCOMM Server stopped.")
    }

    private fun handleClientSocket(socket: BluetoothSocket) {
        try {
            val message = BufferedReader(InputStreamReader(socket.inputStream)).readLine()
            Log.d(TAG, "📩 Received: $message")

            val json = JSONObject(message)
            val studyId = json.getString("studyId")
            val meetingId = json.getString("meetingId")
            val userId = json.getString("userId")

            GlobalScope.launch(Dispatchers.IO) {
                processAttendanceWithRepository(studyId, meetingId, userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling client socket", e)
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "❌ Error closing client socket", e)
            }
        }
    }

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
            Log.d(TAG, "✅ 출석 처리 완료 (Repository) - User: $userId, Meeting: $meetingId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Repository 출석 처리 실패", e)
        }
    }
}