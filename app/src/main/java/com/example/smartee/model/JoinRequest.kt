
package com.example.smartee.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class JoinRequest(
    val studyId: String = "",
    val studyTitle: String = "",
    val requesterId: String = "",
    val requesterNickname: String = "",
    val ownerId: String = "",
    var status: String = "pending", // "pending", "approved", "rejected"
    @ServerTimestamp
    val timestamp: Timestamp? = null
)