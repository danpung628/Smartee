// smartee/bluetooth/BluetoothClientService.kt

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

// [추가] 블루투스 출석 결과를 나타내는 Sealed Class
sealed class AttendanceResult {
    object Success : AttendanceResult()
    data class Failure(val reason: String) : AttendanceResult()
}

class BluetoothClientService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // [수정] 함수의 반환 타입을 AttendanceResult로 변경
    suspend fun sendAttendance(studyId: String, meetingId: String, userId: String): AttendanceResult = withContext(Dispatchers.IO) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return@withContext AttendanceResult.Failure("블루투스가 꺼져 있습니다.")
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return@withContext AttendanceResult.Failure("블루투스 연결 권한이 없습니다.")
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        val targetDevice = pairedDevices?.find {
            it.name.contains("AttendanceServer", ignoreCase = true)
        }

        if (targetDevice == null) {
            return@withContext AttendanceResult.Failure("호스트 기기를 찾을 수 없습니다. 세션이 열려있는지 확인하세요.")
        }

        var socket: BluetoothSocket? = null
        try {
            val uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
            socket = targetDevice.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val writer = OutputStreamWriter(socket.outputStream)
            val json = JSONObject().apply {
                put("studyId", studyId)
                put("meetingId", meetingId)
                put("userId", userId)
            }

            writer.write(json.toString() + "\n")
            writer.flush()

            Log.d("BluetoothClient", "✅ 출석 정보 전송 완료: $json")
            return@withContext AttendanceResult.Success
        } catch (e: Exception) {
            Log.e("BluetoothClient", "❌ 전송 실패", e)
            return@withContext AttendanceResult.Failure("연결에 실패했습니다: ${e.message}")
        } finally {
            socket?.close()
        }
    }
}