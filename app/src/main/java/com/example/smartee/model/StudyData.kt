package com.example.smartee.model

import java.time.LocalDateTime

data class StudyData(
    val studyId:String = "",
    var category:String,
    val date: LocalDateTime = LocalDateTime.now(),
    var title:String,
    var description:String="반갑습니다 ㅎㅎ",
    var address:String,
    var currentMemberCount:Int = 0,
    var maxMemberCount:Int,
    var commentCount:Int = 0,
    var likeCount:Int = 0,
    var thumbnailModel:String
)