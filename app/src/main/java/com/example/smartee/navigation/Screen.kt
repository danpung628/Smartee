package com.example.smartee.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("SignUp")

    //스터디 목록 관련
    object StudyList : Screen("study_list")
    object Search : Screen("Search")
    object Detail : Screen("Detail")

    object StudyCreate : Screen("study_create")
    object Attendance : Screen("attendance")
    object Profile : Screen("profile")

    // 출석 관련 화면
    object Host : Screen("attendance_host")
    object Participant : Screen("attendance_participant")
    object FillProfile : Screen("fill_profile")
}