package com.example.smartee

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
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                1001
            )
        }
    }
}
