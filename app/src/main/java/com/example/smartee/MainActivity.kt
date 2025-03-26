package com.example.smartee

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smartee.auth.ui.screens.LoginScreen  // 임포트 경로 변경
import com.example.smartee.ui.theme.SmarteeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmarteeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            // 여기에 로그인 로직 구현
                            Toast.makeText(
                                this,
                                "로그인 시도: $email",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onRegisterClick = {
                            // 회원가입 화면으로 이동하는 로직
                            Toast.makeText(
                                this,
                                "회원가입 화면으로 이동",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}