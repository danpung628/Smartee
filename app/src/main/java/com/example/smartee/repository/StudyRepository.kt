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
import com.example.smartee.model.UserData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class StudyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studiesCollection = firestore.collection("studies")
    private val joinRequestsCollection = firestore.collection("joinRequests")
    private val usersCollection = firestore.collection("users")
    private val meetingsCollection = firestore.collection("meetings")
    private val attendanceSessionsCollection = firestore.collection("attendanceSessions")
    private val meetingJoinRequestsCollection = firestore.collection("meetingJoinRequests")

    fun listenForMeetingAttendance(
        meeting: Meeting,
        onUpdate: (List<ParticipantStatus>) -> Unit
    ): ListenerRegistration {
        val participantIds = meeting.confirmedParticipants
        if (participantIds.isEmpty()) {
            onUpdate(emptyList())
            // 빈 리스트를 반환할 때도 유효한 리스너 객체를 반환해야 합니다.
            return object : ListenerRegistration {
                override fun remove() {}
            }
        }

        val attendanceCollectionRef =
            meetingsCollection.document(meeting.meetingId).collection("attendance")

        // 참여자들의 프로필 정보를 효율적으로 관리하기 위한 리스너
        return usersCollection.whereIn(FieldPath.documentId(), participantIds)
            .addSnapshotListener { userProfilesSnapshot, userError ->
                if (userError != null || userProfilesSnapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val userMap = userProfilesSnapshot.documents.associate { doc ->
                    doc.id to Pair(
                        doc.getString("nickname") ?: "알 수 없음",
                        doc.getString("thumbnailUrl") ?: ""
                    )
                }

                // 출석 정보 리스너
                attendanceCollectionRef.addSnapshotListener { attendanceSnapshot, attendanceError ->
                    if (attendanceError != null) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }

                    val attendanceMap = attendanceSnapshot?.documents?.associate { doc ->
                        doc.id to (doc.getBoolean("isPresent") == true)
                    } ?: emptyMap()

                    val statuses = participantIds.map { userId ->
                        val (name, thumbnailUrl) = userMap[userId] ?: Pair("알 수 없음", "")
                        ParticipantStatus(
                            userId = userId,
                            name = name,
                            thumbnailUrl = thumbnailUrl, // [수정] thumbnailUrl 추가
                            isPresent = attendanceMap[userId] ?: false
                        )
                    }
                    onUpdate(statuses)
                }
            }
    }


    fun incrementTotalCountForStudy(studyId: String): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val membersRef = db.collection("studies").document(studyId).collection("members")

        return membersRef.get().continueWithTask { task ->
            val memberDocs = task.result?.documents ?: emptyList()

            db.runTransaction { transaction ->
                for (doc in memberDocs) {
                    val memberRef = membersRef.document(doc.id)
                    transaction.update(memberRef, "totalCount", FieldValue.increment(1))
                }
                null
            }
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
        batch.update(
            meetingRef,
            "confirmedParticipants",
            FieldValue.arrayUnion(request.requesterId)
        )
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
        val attendanceRef =
            meetingsCollection.document(meetingId).collection("attendance").document(userId)
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

    fun createAttendanceSession(meetingId: String, code: Int): Task<Void> {
        val sessionData = mapOf("code" to code, "startedAt" to System.currentTimeMillis())
        // 문서 ID로 meetingId를 사용
        return attendanceSessionsCollection.document(meetingId).set(sessionData)
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

        // [추가] 가입 승인 시, 멤버의 출석 기록용 문서를 생성하기 위한 참조
        val memberRef = studyRef.collection("members").document(request.requesterId)

        return firestore.runTransaction { transaction ->
            val studySnapshot = transaction.get(studyRef)
            val userSnapshot = transaction.get(userRef)

            val studyData = studySnapshot.toObject(StudyData::class.java)
                ?: throw Exception("스터디 정보를 찾을 수 없습니다.")
            val userData = userSnapshot.toObject(UserData::class.java)
                ?: throw Exception("사용자 정보를 찾을 수 없습니다.")

            if (userData.ink < studyData.minInkLevel) {
                throw Exception("가입에 필요한 최소 잉크 레벨(${studyData.minInkLevel})을 만족하지 못했습니다.")
            }
            if (userData.pen < studyData.penCount) {
                throw Exception("가입에 필요한 만년필(${studyData.penCount}개)이 부족합니다.")
            }

            val newPenCount = userData.pen - studyData.penCount

            // 사용자 재화 차감 및 참여 스터디 목록 추가
            transaction.update(userRef, "pen", newPenCount)
            transaction.update(userRef, "joinedStudyIds", FieldValue.arrayUnion(request.studyId))

            // 스터디 참여자 목록에 추가
            transaction.update(
                studyRef,
                "participantIds",
                FieldValue.arrayUnion(request.requesterId)
            )

            // [추가] 멤버의 누적 출석 기록용 문서 생성
            val initialMemberData = mapOf(
                "name" to request.requesterNickname,
                "studyName" to request.studyTitle,
                "currentCount" to 0L,
                "totalCount" to 0L,
                "absentCount" to 0L,
                "present" to false
            )
            transaction.set(memberRef, initialMemberData)

            // 가입 요청 상태 변경
            transaction.update(requestRef, "status", "approved")

            null
        }.continueWithTask { task ->
            if (task.isSuccessful) {
                com.google.android.gms.tasks.Tasks.forResult(null)
            } else {
                com.google.android.gms.tasks.Tasks.forException(
                    task.exception ?: Exception("알 수 없는 트랜잭션 오류")
                )
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
            snapshot.documents.mapNotNull {
                it.toObject(StudyData::class.java)?.copy(studyId = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStudiesByIds(studyIds: List<String>): List<StudyData> {
        if (studyIds.isEmpty()) {
            return emptyList()
        }
        return try {
            val snapshot = studiesCollection.whereIn(FieldPath.documentId(), studyIds).get().await()
            snapshot.documents.mapNotNull {
                it.toObject(StudyData::class.java)?.copy(studyId = it.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
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
        } catch (e: Exception) {
            false
        }
    }

    fun createJoinRequest(request: JoinRequest): Task<Void> {
        val newRequestRef = joinRequestsCollection.document()
        return newRequestRef.set(request)
    }

    suspend fun createStudyWithBadgeCheck(studyData: StudyData, ownerId: String) {
        // 1. 기존 스터디 생성 로직 (예시)
        studiesCollection.add(studyData).await()

        // 2. 뱃지 획득 로직 추가
        val userRef = usersCollection.document(ownerId)
        try {
            firestore.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val createdCount = userDoc.getLong("createdStudiesCount") ?: 0
                val newCreatedCount = createdCount + 1

                val earnedBadges =
                    (userDoc.get("earnedBadgeIds") as? List<String> ?: emptyList()).toMutableSet()

                if (newCreatedCount == 1L) {
                    earnedBadges.add("first_study_create") // 예시 뱃지 ID
                }
                if (newCreatedCount == 5L) {
                    earnedBadges.add("five_studies_create") // 예시 뱃지 ID
                }

                transaction.update(userRef, "createdStudiesCount", newCreatedCount)
                transaction.update(userRef, "earnedBadgeIds", earnedBadges.toList())
                null
            }.await()
        } catch (e: Exception) {
            // 오류 처리
        }
    }

    fun withdrawFromMeeting(meetingId: String, userId: String): Task<Void> {
        val meetingRef = meetingsCollection.document(meetingId)
        // confirmedParticipants 배열에서 현재 사용자 ID를 제거
        return meetingRef.update("confirmedParticipants", FieldValue.arrayRemove(userId))
    }

    // [수정] studyId 대신 meetingId를 받도록 변경
    suspend fun getActiveAttendanceSession(meetingId: String): Boolean {
        val thirtyMinutesAgo = System.currentTimeMillis() - 30 * 60 * 1000
        return try {
            // 문서 ID로 meetingId를 사용
            val sessionDoc = attendanceSessionsCollection.document(meetingId).get().await()
            if (sessionDoc.exists()) {
                val startedAt = sessionDoc.getLong("startedAt") ?: 0L
                startedAt > thirtyMinutesAgo
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // [수정] 문서를 읽고, 상태에 따라 생성 또는 업데이트를 모두 처리하는 트랜잭션 방식으로 변경
    fun markAttendance(
        meetingId: String,
        userId: String,
        parentStudyId: String,
        userName: String,
        studyName: String
    ): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val attendanceRef =
            meetingsCollection.document(meetingId).collection("attendance").document(userId)
        val memberRef =
            studiesCollection.document(parentStudyId).collection("members").document(userId)

        return db.runTransaction { transaction ->
            val memberDoc = transaction.get(memberRef)

            // 1. 세부 모임의 출석부에 출석 기록
            transaction.set(attendanceRef, mapOf("isPresent" to true), SetOptions.merge())

            // 2. 스터디 전체의 누적 출석 횟수 처리
            if (memberDoc.exists()) {
                // 문서가 존재하면, 출석 횟수(currentCount)만 1 증가
                transaction.update(memberRef, "currentCount", FieldValue.increment(1))
            } else {
                // 문서가 없으면, 새로 생성 (안전장치)
                val initialMemberData = mapOf(
                    "name" to userName,
                    "studyName" to studyName,
                    "currentCount" to 1L,
                    "totalCount" to 0L, // totalCount는 세션 시작 시점에 오르므로 여기선 0
                    "absentCount" to 0L,
                    "present" to true
                )
                transaction.set(memberRef, initialMemberData)
            }
            null
        }
    }

    // smartee/repository/StudyRepository.kt

    // [추가] 관리자 ID로 모든 보류중인 가입 요청을 가져오는 함수
    suspend fun getRequestsForOwner(ownerId: String): List<JoinRequest> {
        return try {
            val snapshot = joinRequestsCollection
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.toObjects(JoinRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // [추가] 관리자 ID로 보류중인 가입 요청의 개수만 가져오는 함수 (알림용)
    suspend fun getPendingRequestCountForOwner(ownerId: String): Int {
        return try {
            val snapshot = joinRequestsCollection
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("status", "pending")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }



}