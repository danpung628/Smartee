package com.example.smartee

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.smartee.ui.SmarteeApp
import com.example.smartee.ui.theme.SmarteeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBluetoothPermissionIfNeeded()
        // 앱 컨텐츠가 상태바 영역을 침범하지 않도록 설정
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            SmarteeTheme {
                // 앱 내용
                SmarteeApp()
            }
        }
    }
    private fun requestBluetoothPermissionIfNeeded() {
        // [수정] 필요한 모든 블루투스 권한 목록
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION // cũ 버전에서는 위치 권한이 필요
            )
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                1001 // 요청 코드
            )
        }
    }
}
