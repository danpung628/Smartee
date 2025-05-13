package com.example.smartee.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("SignUp")

    object StudyList : Screen("study_list")
    object Search : Screen("Search")
    object Detail : Screen("Detail")

    object StudyCreate : Screen("study_create")
    object Attendance : Screen("attendance")
    object Profile : Screen("profile")
    object FillProfile : Screen("fill_profile")
}