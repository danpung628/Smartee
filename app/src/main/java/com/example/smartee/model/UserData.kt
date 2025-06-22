package com.example.smartee.model

data class UserData(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val nickname: String = "",
    val age: Int = 0,
    val gender: String = "",
    val region: String = "",
    val interests: List<String> = emptyList(),
    val ink: Int = 0,
    val pen: Int = 0,
    val createdStudyIds: List<String> = emptyList(),
    val joinedStudyIds: List<String> = emptyList(),

    // ▼▼▼ 뱃지 시스템을 위해 추가된 필드들 ▼▼▼
    val earnedBadgeIds: List<String> = emptyList(),
    val completedStudiesCount: Int = 0,
    val createdStudiesCount: Int = 0,
    val perfectAttendanceCount: Int = 0
)