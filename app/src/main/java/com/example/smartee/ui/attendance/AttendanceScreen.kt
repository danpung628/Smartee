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
        Text("역할을 선택하세요", fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(Screen.Host.route) }) {
            Text("관리자 출석")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { navController.navigate(Screen.Participant.route) }) {
            Text("참여자 출석")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AttendanceScreenPreview() {
    AttendanceScreen(navController = rememberNavController())
}