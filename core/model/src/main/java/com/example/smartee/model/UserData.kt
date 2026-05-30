package com.example.smartee.model

// [수정] 모든 프로퍼티에 @JvmField 어노테이션을 추가합니다.
data class UserData(
    @JvmField
    val uid: String = "",
    @JvmField
    val email: String = "",
    @JvmField
    val name: String = "",
    @JvmField
    val photoUrl: String = "",
    @JvmField
    val nickname: String = "",
    @JvmField
    val age: Int = 0,
    @JvmField
    val gender: String = "",
    @JvmField
    val region: String = "",
    @JvmField
    val interests: List<String> = emptyList(),
    @JvmField
    val ink: Int = 0,
    @JvmField
    val pen: Int = 0,
    @JvmField
    val createdStudyIds: List<String> = emptyList(),
    @JvmField
    val joinedStudyIds: List<String> = emptyList(),
    @JvmField
    val earnedBadgeIds: List<String> = emptyList(),
    @JvmField
    val completedStudiesCount: Int = 0,
    @JvmField
    val createdStudiesCount: Int = 0,
    @JvmField
    val perfectAttendanceCount: Int = 0
)