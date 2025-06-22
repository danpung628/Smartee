// smartee/repository/StudyRepository.kt

package com.example.smartee.repository

import com.example.smartee.model.JoinRequest
import com.example.smartee.model.Meeting
import com.example.smartee.model.MeetingJoinRequest
import com.example.smartee.model.ParticipantStatus
import com.example.smartee.model.StudyData
import com.example.smartee.ui.attendance.AttendanceInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class StudyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studiesCollection = firestore.collection("studies")
    private val joinRequestsCollection = firestore.collection("joinRequests")
    private val usersCollection = firestore.collection("users")
    private val meetingsCollection = firestore.collection("meetings")
    private val attendanceSessionsCollection = firestore.collection("attendanceSessions")
    private val meetingJoinRequestsCollection = firestore.collection("meetingJoinRequests")

    suspend fun getMeetingAttendanceStatus(meeting: Meeting): List<ParticipantStatus> {
        val participantIds = meeting.confirmedParticipants
        if (participantIds.isEmpty()) return emptyList()

        return try {
            coroutineScope {
                val userProfileJobs = participantIds.map { userId ->
                    async { usersCollection.document(userId).get().await() }
                }
                val userProfiles = userProfileJobs.awaitAll()

                val attendanceJobs = participantIds.map { userId ->
                    async { meetingsCollection.document(meeting.meetingId).collection("attendance").document(userId).get().await() }
                }
                val attendanceSnaps = attendanceJobs.awaitAll()

                participantIds.mapIndexed { index, userId ->
                    val userProfile = userProfiles[index]
                    val attendanceSnap = attendanceSnaps[index]
                    ParticipantStatus(
                        userId = userId,
                        name = userProfile.getString("nickname") ?: "알 수 없음",
                        thumbnailUrl = userProfile.getString("profileImageUrl") ?: "",
                        isPresent = attendanceSnap.exists() && attendanceSnap.getBoolean("isPresent") == true
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingRequestsForMeeting(meetingId: String): List<MeetingJoinRequest> {
        return try {
            val snapshot = meetingJoinRequestsCollection
                .whereEqualTo("meetingId", meetingId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.toObjects(MeetingJoinRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingMeetingRequestsForStudy(studyId: String): List<MeetingJoinRequest> {
        return try {
            val snapshot = meetingJoinRequestsCollection
                .whereEqualTo("studyId", studyId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.toObjects(MeetingJoinRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun approveMeetingJoinRequest(request: MeetingJoinRequest): Task<Void> {
        val batch = firestore.batch()
        val requestRef = meetingJoinRequestsCollection.document(request.requestId)
        batch.update(requestRef, "status", "approved")
        val meetingRef = meetingsCollection.document(request.meetingId)
        batch.update(meetingRef, "confirmedParticipants", FieldValue.arrayUnion(request.requesterId))
        return batch.commit()
    }

    fun rejectMeetingJoinRequest(requestId: String): Task<Void> {
        return meetingJoinRequestsCollection.document(requestId).update("status", "rejected")
    }

    fun createMeetingJoinRequest(request: MeetingJoinRequest): Task<Void> {
        return meetingJoinRequestsCollection.document().set(request)
    }

    fun addCurrentUserToMeeting(meetingId: String, userId: String): Task<Void> {
        val meetingRef = meetingsCollection.document(meetingId)
        return meetingRef.update("confirmedParticipants", FieldValue.arrayUnion(userId))
    }

    fun markUserAsPresent(meetingId: String, userId: String): Task<Void> {
        val attendanceRef = meetingsCollection.document(meetingId).collection("attendance").document(userId)
        return attendanceRef.set(mapOf("isPresent" to true))
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
                AttendanceInfo(
                    userId = doc.id,
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
        val sessionData = mapOf("code" to code, "startedAt" to System.currentTimeMillis())
        return attendanceSessionsCollection.document(studyId).set(sessionData)
    }
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
    suspend fun updateMeeting(meetingId: String, meetingData: Map<String, Any>): Task<Void> {
        val meetingRef = meetingsCollection.document(meetingId)
        val batch = firestore.batch()

        val oldMeetingSnapshot = meetingRef.get().await()
        val oldMeeting = oldMeetingSnapshot.toObject(Meeting::class.java)

        if (oldMeeting != null) {
            val oldSummary = mapOf(
                "meetingId" to oldMeeting.meetingId,
                "title" to oldMeeting.title,
                "date" to oldMeeting.date
            )
            val parentStudyRef = studiesCollection.document(oldMeeting.parentStudyId)
            batch.update(parentStudyRef, "meetingSummaries", FieldValue.arrayRemove(oldSummary))
        }

        val newSummary = mapOf(
            "meetingId" to meetingId,
            "title" to (meetingData["title"] ?: ""),
            "date" to (meetingData["date"] ?: "")
        )
        val parentStudyId = meetingData["parentStudyId"] as? String ?: oldMeeting?.parentStudyId
        if (parentStudyId != null) {
            val parentStudyRef = studiesCollection.document(parentStudyId)
            batch.update(parentStudyRef, "meetingSummaries", FieldValue.arrayUnion(newSummary))
        }

        batch.update(meetingRef, meetingData)

        return batch.commit()
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
    suspend fun approveJoinRequest(request: JoinRequest): Task<Void> {
        val studyRef = studiesCollection.document(request.studyId)
        val userRef = usersCollection.document(request.requesterId)
        val requestRef = joinRequestsCollection.document(request.requestId)

        // runTransaction을 사용하여 여러 데이터 읽기/쓰기를 원자적으로 처리
        return firestore.runTransaction { transaction ->
            // 1. 스터디 정보와 사용자 정보를 트랜잭션 안에서 읽기
            val studySnapshot = transaction.get(studyRef)
            val userSnapshot = transaction.get(userRef)

            val studyData = studySnapshot.toObject(StudyData::class.java)
                ?: throw Exception("스터디 정보를 찾을 수 없습니다.")
            val userData = userSnapshot.toObject(UserData::class.java) // UserData 모델이 있다고 가정
                ?: throw Exception("사용자 정보를 찾을 수 없습니다.")

            // 2. 가입 조건 및 재화 확인
            // 2-1. 잉크 레벨 조건 확인
            if (userData.ink < studyData.minInkLevel) {
                throw Exception("가입에 필요한 최소 잉크 레벨(${studyData.minInkLevel})을 만족하지 못했습니다.")
            }
            // 2-2. 만년필(비용) 확인
            if (userData.pen < studyData.penCount) {
                throw Exception("가입에 필요한 만년필(${studyData.penCount}개)이 부족합니다.")
            }

            // 3. 재화 차감 및 데이터 업데이트
            val newPenCount = userData.pen - studyData.penCount

            // 3-1. 사용자 재화(만년필) 차감 및 참여 스터디 목록 추가
            transaction.update(userRef, "pen", newPenCount)
            transaction.update(userRef, "joinedStudyIds", FieldValue.arrayUnion(request.studyId))

            // 3-2. 스터디 참여자 목록에 추가
            transaction.update(studyRef, "participantIds", FieldValue.arrayUnion(request.requesterId))

            // 3-3. 가입 요청 상태 변경
            transaction.update(requestRef, "status", "approved")

            // 트랜잭션 성공 시 null 반환
            null
        }.continueWithTask { task ->
            if (task.isSuccessful) {
                // Firestore 트랜잭션이 성공적으로 완료되었을 때의 Task 반환
                com.google.android.gms.tasks.Tasks.forResult(null)
            } else {
                // 실패 시 예외를 포함하는 Task 반환
                com.google.android.gms.tasks.Tasks.forException(task.exception ?: Exception("알 수 없는 트랜잭션 오류"))
            }
        }
    }
    fun rejectJoinRequest(requestId: String): Task<Void> {
        val requestRef = joinRequestsCollection.document(requestId)
        return requestRef.update("status", "rejected")
    }
    suspend fun getStudyById(studyId: String): StudyData? {
        return try {
            val studySnapshot = studiesCollection.document(studyId).get().await()
            val study = studySnapshot.toObject(StudyData::class.java)

            if (study != null) {
                val ownerSnapshot = usersCollection.document(study.ownerId).get().await()
                val ownerNickname = ownerSnapshot.getString("nickname") ?: ""
                study.copy(studyId = studySnapshot.id, ownerNickname = ownerNickname)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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