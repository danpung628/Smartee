
package com.example.smartee.model

import com.google.firebase.firestore.DocumentId

data class MeetingJoinRequest(
    @DocumentId
    val requestId: String = "",
    val meetingId: String = "",
    val meetingTitle: String = "",
    val studyId: String = "",
    val studyOwnerId: String = "",
    val requesterId: String = "",
    val requesterNickname: String = "",
    val status: String = "pending" // "pending", "approved", "rejected"
)