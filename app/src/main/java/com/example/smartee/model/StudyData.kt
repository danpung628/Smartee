package com.example.smartee.model

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class StudyData(
    val ownerId: String = "",
    val participantIds: List<String> = emptyList(),
    val ownerNickname: String = "", // [추가]



    val studyId: String = "",
    var title: String = "",
    var category: String = "",
    //등록일
    val dateTimestamp: Timestamp = Timestamp.now(),
    //스터디 기간
    var startDate: String = "",
    var endDate: String = "",
    //정기/비정기
    var isRegular: Boolean = false,
// 정기 스터디의 경우
    var regularDays: List<String> = listOf(), // "월", "수", "금" 등의 요일
    var regularTime: String = "", // "19:00~21:00" 형식의 시간

// 비정기 스터디의 경우
    var irregularDateTimes: List<String> = listOf(), // "2025-05-20 15:00~17:00" 등의 날짜/시간

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
    val meetingSummaries: List<Map<String, String>> = emptyList(), // "id", "title", "date" 요약 정보
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