package com.example.smartee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartee.ui.attendance.AttendanceScreen
import com.example.smartee.ui.login.LoginScreen
import com.example.smartee.ui.profile.ProfileScreen
import com.example.smartee.ui.signup.FillProfileScreen
import com.example.smartee.ui.signup.SignUpScreen
import com.example.smartee.ui.study.StudyCreateScreen
import com.example.smartee.ui.study.StudyListScreen

@Composable
fun SmarteeNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.SignUp.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.FillProfile.route) {
            FillProfileScreen(navController)
        }
        composable(Screen.StudyCreate.route) {
            StudyCreateScreen(navController)
        }
        composable(Screen.StudyList.route) {
            StudyListScreen(navController)
        }
        composable(Screen.Attendance.route) {
            AttendanceScreen(navController)
        }
    }
}
