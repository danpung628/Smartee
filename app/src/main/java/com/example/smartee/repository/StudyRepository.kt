package com.example.smartee.repository

import com.example.smartee.model.JoinRequest
import com.example.smartee.model.Meeting
import com.example.smartee.model.StudyData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class StudyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studiesCollection = firestore.collection("studies")
    private val joinRequestsCollection = firestore.collection("joinRequests")
    private val usersCollection = firestore.collection("users")
    private val meetingsCollection = firestore.collection("meetings")

    fun createMeeting(meeting: Meeting): Task<Void> {
        val batch = firestore.batch()
        val newMeetingRef = meetingsCollection.document()

        // Firestore에 저장할 데이터를 Map 형태로 수동으로 만듭니다.
        val meetingData = hashMapOf(
            "parentStudyId" to meeting.parentStudyId,
            "title" to meeting.title,
            "date" to meeting.date,
            "time" to meeting.time,
            "isOffline" to meeting.isOffline,
            "location" to meeting.location,
            "description" to meeting.description,
            "maxParticipants" to meeting.maxParticipants,
            "applicants" to meeting.applicants,
            "confirmedParticipants" to meeting.confirmedParticipants,
            "timestamp" to FieldValue.serverTimestamp() // 서버 시간을 사용해 시간 불일치 방지
        )
        // 객체가 아닌 Map 데이터를 저장합니다.
        batch.set(newMeetingRef, meetingData)

        // studies 문서에 요약 정보를 업데이트하는 로직은 동일합니다.
        val parentStudyRef = studiesCollection.document(meeting.parentStudyId)
        val meetingSummary = mapOf(
            "meetingId" to newMeetingRef.id,
            "title" to meeting.title,
            "date" to meeting.date
        )
        batch.update(parentStudyRef, "meetingSummaries", FieldValue.arrayUnion(meetingSummary))

        return batch.commit()
    }

    suspend fun getMeetingsForStudy(studyId: String): List<Meeting> {
        return try {
            val snapshot = meetingsCollection
                .whereEqualTo("parentStudyId", studyId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(Meeting::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingRequestsForStudy(studyId: String): List<JoinRequest> {
        return try {
            val snapshot = joinRequestsCollection
                .whereEqualTo("studyId", studyId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.toObjects(JoinRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun approveJoinRequest(request: JoinRequest): Task<Void> {
        val batch = firestore.batch()

        val requestRef = joinRequestsCollection.document(request.requestId)
        batch.update(requestRef, "status", "approved")

        val studyRef = studiesCollection.document(request.studyId)
        batch.update(studyRef, "participantIds", FieldValue.arrayUnion(request.requesterId))

        val userRef = usersCollection.document(request.requesterId)
        batch.update(userRef, "joinedStudyIds", FieldValue.arrayUnion(request.studyId))

        return batch.commit()
    }

    fun rejectJoinRequest(requestId: String): Task<Void> {
        val requestRef = joinRequestsCollection.document(requestId)
        return requestRef.update("status", "rejected")
    }

    suspend fun getStudyById(studyId: String): StudyData? {
        return try {
            val snapshot = studiesCollection.document(studyId).get().await()
            snapshot.toObject(StudyData::class.java)?.copy(studyId = snapshot.id)
        } catch (e: Exception) { null }
    }

    suspend fun getAllStudies(): List<StudyData> {
        return try {
            val snapshot = studiesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(StudyData::class.java)?.copy(studyId = it.id) }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getStudiesByIds(studyIds: List<String>): List<StudyData> {
        if (studyIds.isEmpty()) { return emptyList() }
        return try {
            val snapshot = studiesCollection.whereIn(FieldPath.documentId(), studyIds).get().await()
            snapshot.documents.mapNotNull { it.toObject(StudyData::class.java)?.copy(studyId = it.id) }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun checkIfRequestExists(userId: String, studyId: String): Boolean {
        return try {
            val snapshot = joinRequestsCollection
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("studyId", studyId)
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) { false }
    }

    fun createJoinRequest(request: JoinRequest): Task<Void> {
        val newRequestRef = joinRequestsCollection.document()
        return newRequestRef.set(request)
    }
}