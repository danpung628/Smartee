// smartee/bluetooth/BluetoothClientService.kt

package com.example.smartee.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.util.*

sealed class AttendanceResult {
    object Success : AttendanceResult()
    data class Failure(val reason: String) : AttendanceResult()
}

class BluetoothClientService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bleScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let { device ->
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    if (device.name != null && !_discoveredDevices.value.any { it.address == device.address }) {
                        _discoveredDevices.value = _discoveredDevices.value + device
                        Log.d("BluetoothClient", "✅ BLE device found: ${device.name} - ${device.address}")
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothClient", "❌ BLE Scan failed with error code: $errorCode")
            _isScanning.value = false
        }
    }

    fun startScan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BluetoothClient", "BLUETOOTH_SCAN permission not granted.")
            return
        }

        _discoveredDevices.value = emptyList()

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(BluetoothServerService.SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner?.startScan(listOf(scanFilter), settings, scanCallback)
        _isScanning.value = true
        Log.d("BluetoothClient", "BLE Scan started for UUID: ${BluetoothServerService.SERVICE_UUID}")
    }

    fun stopScan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bleScanner?.stopScan(scanCallback)
        _isScanning.value = false
        Log.d("BluetoothClient", "BLE Scan stopped.")
    }

    suspend fun sendAttendance(device: BluetoothDevice, studyId: String, meetingId: String, userId: String): AttendanceResult = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return@withContext AttendanceResult.Failure("블루투스 연결 권한이 없습니다.")
        }

        stopScan() // 연결 시도 전 스캔 중지
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(BluetoothServerService.SERVICE_UUID)
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
            Log.e("BluetoothClient", "❌ RFCOMM 전송 실패", e)
            return@withContext AttendanceResult.Failure("연결에 실패했습니다. 호스트와의 거리를 확인해주세요.")
        } finally {
            socket?.close()
        }
    }
}