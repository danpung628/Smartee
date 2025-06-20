package com.example.smartee.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartee.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { navController.navigate(Screen.Profile.route) }) {
            Text("Go to Profile")
        }
        Button(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text("Go to Sign Up")
        }
        Button(onClick = { navController.navigate(Screen.StudyCreate.route) }) {
            Text("Go to Study Create")
        }
        Button(onClick = { navController.navigate(Screen.StudyList.route) }) {
            Text("Go to Study List")
        }
        Button(onClick = { navController.navigate(Screen.Attendance.route) }) {
            Text("Go to Attendance")
        }
        Button(onClick = { navController.navigate("map") }) {  // ✅ 지도 화면으로 이동하는 버튼
            Text("Go to Map")
        }
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Screen.SignUp.route)
        }) {
            Text("로그아웃")
        }
    }
}
