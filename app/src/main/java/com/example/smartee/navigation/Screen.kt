package com.example.smartee.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object StudyList : Screen("study_list")
    object StudyCreate : Screen("study_create")
    object Attendance : Screen("attendance")
    object Profile : Screen("profile")
}