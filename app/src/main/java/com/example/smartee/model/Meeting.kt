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
    val location: String = "",
    val description: String = "",
    val attendees: List<String> = emptyList()
)