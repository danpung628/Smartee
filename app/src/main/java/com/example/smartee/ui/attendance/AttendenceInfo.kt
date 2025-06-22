package com.example.smartee.ui.attendance

data class AttendanceInfo(
    val userId: String,
    val studyName: String,
    val name: String,
    val isPresent: Boolean,
    val currentCount: Int,
    val totalCount: Int,
    val absentCount: Int
)