package com.example.smartee.model

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class StudyData(
    val studyId: String = "",
    var title: String = "",
    var category: String = "",
    //등록일
    val dateTimestamp: Timestamp = Timestamp.now(),
    //스터디 기간
    var startDate: String = "",
    var endDate: String = "",
    var currentMemberCount: Int = 0,
    var maxMemberCount: Int = 0,
    var isOffline: Boolean = true,
    var description: String = "반갑습니다 ㅎㅎ",
    var address: String = "",
    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var thumbnailModel: String = ""
) {
    // LocalDateTime 변환 헬퍼 함수
    fun getLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateTimestamp.seconds * 1000),
            ZoneId.systemDefault()
        )
    }
}