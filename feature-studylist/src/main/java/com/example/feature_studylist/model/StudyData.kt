package com.example.feature_studylist.model

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class StudyData(
    var title:String,
    var address:String,
    val date: LocalDateTime = LocalDateTime.now(),
    var currentMemberCount:Int = 0,
    var maxMemberCount:Int
)