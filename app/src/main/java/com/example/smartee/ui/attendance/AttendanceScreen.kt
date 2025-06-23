package com.example.smartee.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartee.navigation.Screen

@Composable
fun AttendanceScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("출석 관리", fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(modifier = Modifier.height(24.dp))

        // [수정] 관리자 출석 버튼만 남깁니다.
        // 이 버튼은 아마도 관리자가 출석 세션을 여는 화면으로 연결될 것입니다.
        // 현재 HostScreen이 구현되지 않았으므로, 동작은 추후 정의가 필요합니다.
        Button(
            onClick = {
                // TODO: 관리자용 출석 관리 화면(직접 세션을 여는)으로 네비게이션
                // 예: navController.navigate(Screen.Host.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("스터디 출석 세션 관리")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AttendanceScreenPreview() {
    AttendanceScreen(navController = rememberNavController())
}