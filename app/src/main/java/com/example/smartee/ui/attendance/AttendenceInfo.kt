package com.example.smartee.ui.attendance

data class AttendanceInfo(
    val userId: String = "",
    val studyName: String = "",
    val name: String = "",
    val isPresent: Boolean = false,
    val currentCount: Int = 0,
    val totalCount: Int = 0,
    val absentCount: Int = 0

)