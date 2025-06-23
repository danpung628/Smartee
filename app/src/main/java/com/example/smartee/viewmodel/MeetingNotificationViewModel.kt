package com.example.smartee.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.smartee.model.Meeting
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class MeetingNotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "MeetingNotifier"

    fun listenForApplications(onApplicantDetected: (Meeting) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val currentUid = currentUser.uid

        db.collection("meetings")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Firestore listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (change in snapshot.documentChanges) {
                        if (change.type == DocumentChange.Type.MODIFIED) {
                            val meeting = change.document.toObject(Meeting::class.java)

                            // 내 스터디 소속인지 확인
                            db.collection("studies")
                                .document(meeting.parentStudyId)
                                .get()
                                .addOnSuccessListener { studyDoc ->
                                    val ownerId = studyDoc.getString("ownerId")
                                    if (ownerId == currentUid) {
                                        Log.d(TAG, "참가 신청 감지된 미팅: ${meeting.title}")
                                        onApplicantDetected(meeting)
                                    }
                                }
                        }
                    }
                }
            }
    }
}
