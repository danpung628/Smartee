// smartee/bluetooth/BluetoothClientService.kt

package com.example.smartee.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
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

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            if (it.name != null && !_discoveredDevices.value.any { d -> d.address == it.address }) {
                                _discoveredDevices.value = _discoveredDevices.value + it
                                Log.d("BluetoothClient", "Device found: ${it.name} - ${it.address}")
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                    Log.d("BluetoothClient", "Discovery finished.")
                }
            }
        }
    }

    fun startDiscovery() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BluetoothClient", "BLUETOOTH_SCAN permission not granted.")
            return
        }
        // [추가] 검색 시작 전, 만약을 위해 이전 검색을 확실히 취소합니다.
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        _discoveredDevices.value = emptyList()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
        _isScanning.value = true
        Log.d("BluetoothClient", "Discovery started.")
    }

    fun stopDiscovery() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
        _isScanning.value = false
        Log.d("BluetoothClient", "Discovery stopped.")
    }

    suspend fun sendAttendance(device: BluetoothDevice, studyId: String, meetingId: String, userId: String): AttendanceResult = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return@withContext AttendanceResult.Failure("블루투스 연결 권한이 없습니다.")
        }
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        var socket: BluetoothSocket? = null
        try {
            val uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
            socket = device.createRfcommSocketToServiceRecord(uuid)
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
            return@withContext AttendanceResult.Failure("연결에 실패했습니다. 호스트가 세션을 열었는지 확인하세요.")
        } finally {
            socket?.close()
        }
    }
}