package com.example.smartee.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    //스터디 목록 관련
    object StudyList : Screen("study_list")
    object Search : Screen("Search")
    object Detail : Screen("Detail")

    object StudyCreate : Screen("study_create")
    object Attendance : Screen("attendance")
    object Profile : Screen("profile")
}