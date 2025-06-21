package com.example.smartee.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Meeting(
    @DocumentId
    val meetingId: String = "",
    val parentStudyId: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "", // "19:00~21:00" 형식
    val isOffline: Boolean = true,
    val location: String = "",
    val description: String = "",
    val maxParticipants: Int = 0,
    val applicants: List<String> = emptyList(), // 참여 신청자 목록 (추후 확장용)
    val confirmedParticipants: List<String> = emptyList(), // 확정된 참여자 목록 (추후 확장용)
    val timestamp: Timestamp = Timestamp.now()
)