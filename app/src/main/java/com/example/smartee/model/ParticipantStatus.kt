
package com.example.smartee.model

data class ParticipantStatus(
    val userId: String,
    val name: String,
    val thumbnailUrl: String, // 사용자 프로필 이미지 URL
    val isPresent: Boolean
)