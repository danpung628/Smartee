package com.example.smartee.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class JoinRequest(
    @DocumentId
    val requestId: String = "", // Firestore 문서 ID를 이 필드에 자동으로 매핑합니다.
    val studyId: String = "",
    val studyTitle: String = "",
    val requesterId: String = "",
    val requesterNickname: String = "",
    val ownerId: String = "",
    var status: String = "pending", // "pending", "approved", "rejected"
    val timestamp: Timestamp? = null
)