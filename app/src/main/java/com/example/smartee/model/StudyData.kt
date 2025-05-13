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
    //정기/비정기
    var isRegular:Boolean = false,

    var currentMemberCount: Int = 0,
    var maxMemberCount: Int = 0,
    var isOffline: Boolean = true,
    //잉크 조건
    var minInkLevel: Int = 0,
    //차감 만년필
    var penCount: Int = 0,
    //벌칙
    var punishment: String = "",
    //스터디 설명
    var description: String = "반갑습니다 ㅎㅎ",

    var address: String = "",
    //스터디 댓글
    var commentCount: Int = 0,
    //스터디 좋아요
    var likeCount: Int = 0,
    //스터디 썸네일
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