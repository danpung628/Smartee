// smartee/repository/StudyRepository.kt

package com.example.smartee.repository

import com.example.smartee.model.JoinRequest
import com.example.smartee.model.Meeting
import com.example.smartee.model.StudyData
import com.example.smartee.ui.attendance.AttendanceInfo
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
    private val attendanceSessionsCollection = firestore.collection("attendanceSessions")

    fun markUserAsPresent(studyId: String, userId: String): Task<Void> {
        val memberRef = studiesCollection.document(studyId).collection("members").document(userId)
        val batch = firestore.batch()

        batch.update(memberRef, "present", true)
        batch.update(memberRef, "currentCount", FieldValue.increment(1))

        return batch.commit()
    }

    suspend fun getAttendanceInfoForStudy(studyId: String): List<AttendanceInfo> {
        return try {
            val snapshot = studiesCollection
                .document(studyId)
                .collection("members")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val userData = usersCollection.document(doc.id).get().await()
                val name = userData.getString("name") ?: "이름없음"

                // [수정] AttendanceInfo 생성 시 userId 필드 추가
                AttendanceInfo(
                    userId = doc.id, // 문서의 ID를 userId로 사용
                    studyName = doc.getString("studyName") ?: "",
                    name = name,
                    isPresent = doc.getBoolean("present") ?: false,
                    currentCount = (doc.getLong("currentCount") ?: 0).toInt(),
                    totalCount = (doc.getLong("totalCount") ?: 0).toInt(),
                    absentCount = (doc.getLong("absentCount") ?: 0).toInt()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun createAttendanceSession(studyId: String, code: Int): Task<Void> {
        val sessionData = mapOf(
            "code" to code,
            "startedAt" to System.currentTimeMillis()
        )

        return attendanceSessionsCollection
            .document(studyId)
            .set(sessionData)
    }

    // 이하 다른 함수들은 기존과 동일합니다...
    fun createMeeting(meetingData: Map<String, Any>, parentStudyId: String): Task<Void> {
        val batch = firestore.batch()
        val newMeetingRef = meetingsCollection.document()
        batch.set(newMeetingRef, meetingData)
        val parentStudyRef = studiesCollection.document(parentStudyId)
        val meetingSummary = mapOf(
            "meetingId" to newMeetingRef.id,
            "title" to (meetingData["title"] ?: ""),
            "date" to (meetingData["date"] ?: "")
        )
        batch.update(parentStudyRef, "meetingSummaries", FieldValue.arrayUnion(meetingSummary))
        return batch.commit()
    }

    suspend fun getMeetingById(meetingId: String): Meeting? {
        return try {
            meetingsCollection.document(meetingId).get().await().toObject(Meeting::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateMeeting(meetingId: String, meetingData: Map<String, Any>): Task<Void> {
        return meetingsCollection.document(meetingId).update(meetingData)
    }

    fun deleteMeeting(meeting: Meeting): Task<Void> {
        val batch = firestore.batch()
        val meetingRef = meetingsCollection.document(meeting.meetingId)
        batch.delete(meetingRef)
        val parentStudyRef = studiesCollection.document(meeting.parentStudyId)
        val meetingSummary = mapOf(
            "meetingId" to meeting.meetingId,
            "title" to meeting.title,
            "date" to meeting.date
        )
        batch.update(parentStudyRef, "meetingSummaries", FieldValue.arrayRemove(meetingSummary))
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
    fun addCurrentUserToMeeting(meetingId: String, userId: String): Task<Void> {
        val meetingRef = meetingsCollection.document(meetingId)
        return meetingRef.update("confirmedParticipants", FieldValue.arrayUnion(userId))
    }
    fun createJoinRequest(request: JoinRequest): Task<Void> {
        val newRequestRef = joinRequestsCollection.document()
        return newRequestRef.set(request)
    }
}