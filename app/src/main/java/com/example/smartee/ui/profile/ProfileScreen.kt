package com.example.smartee.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.smartee.navigation.Screen
import com.example.smartee.ui.LocalAuthViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val authViewModel = LocalAuthViewModel.current
    val currentUser by authViewModel.currentUser.collectAsState()

    Column {
        if (currentUser != null) {
            Text("환영합니다, ${currentUser?.displayName}님!")
            // 프로필 정보 표시
        } else {
            Text("로그인이 필요합니다")
            Button(onClick = { navController.navigate(Screen.SignUp.route) }) {
                Text("로그인 하러 가기")
            }
        }
    }
}
