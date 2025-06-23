package com.example.smartee.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.smartee.navigation.Screen
import com.example.smartee.ui.LocalAuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

// SplashScreen.kt
@Composable
fun SplashScreen(navController: NavHostController) {
    val authViewModel = LocalAuthViewModel.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isInitialized by authViewModel.isInitialized.collectAsState()

    LaunchedEffect(isInitialized, currentUser) {
        if (isInitialized) {
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser!!.uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            navController.navigate(Screen.StudyList.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            // 여기를 FillProfile에서 SignUp으로 변경
                            navController.navigate(Screen.SignUp.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
            } else {
                navController.navigate(Screen.SignUp.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    // 로딩 화면 UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}