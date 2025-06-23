// BluetoothClientService.kt
package com.example.smartee.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.util.*

class BluetoothClientService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    suspend fun sendAttendance(studyId: String, userId: String) = withContext(Dispatchers.IO) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w("BluetoothClient", "❌ Bluetooth is unavailable or turned off.")
            return@withContext
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("BluetoothClient", "❌ BLUETOOTH_CONNECT permission not granted.")
            return@withContext
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        val targetDevice = pairedDevices?.find {
            it.name.contains("AttendanceServer", ignoreCase = true)
        }

        if (targetDevice == null) {
            Log.w("BluetoothClient", "❌ Target device not found.")
            return@withContext
        }

        try {
            val uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
            val socket: BluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val writer = OutputStreamWriter(socket.outputStream)
            val json = JSONObject().apply {
                put("studyId", studyId)
                put("userId", userId)
            }

            writer.write(json.toString() + "\n")
            writer.flush()

            Log.d("BluetoothClient", "✅ 출석 정보 전송 완료")
            socket.close()
        } catch (e: Exception) {
            Log.e("BluetoothClient", "❌ 전송 실패", e)
        }
    }
}