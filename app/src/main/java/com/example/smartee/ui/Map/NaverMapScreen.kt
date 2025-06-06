package com.example.smartee.ui.Map

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.map.MapView
import com.naver.maps.map.util.FusedLocationSource
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.naver.maps.map.LocationTrackingMode

@Composable
fun NaverMapScreen(
    onSetLocation: () -> Unit = {} // 클릭 시 처리할 동작 람다
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // 위치 권한 요청
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    getMapAsync { naverMap ->
                        val locationSource = FusedLocationSource(activity!!, LOCATION_PERMISSION_REQUEST_CODE)
                        naverMap.locationSource = locationSource
                        naverMap.locationTrackingMode = LocationTrackingMode.Follow
                    }
                }
            }
        )

        // 최하단 중앙 버튼
        Button(
            onClick = { onSetLocation() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp) // 하단 여백
        ) {
            Text("내 관심위치로 설정")
        }
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
