// smartee/viewmodel/MyStudyViewModel.kt

package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.Meeting
import com.example.smartee.model.StudyData
import com.example.smartee.model.UserData
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import com.example.smartee.ui.attendance.AttendanceInfo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

class MyStudyViewModel : ViewModel() {

    private val studyRepository = StudyRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())

    private val _meetingAttendance = MutableStateFlow<List<AttendanceInfo>>(emptyList())
    val meetingAttendance: StateFlow<List<AttendanceInfo>> = _meetingAttendance

    private val _myCreatedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myCreatedStudies: StateFlow<List<StudyData>> = _myCreatedStudies

    private val _myJoinedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myJoinedStudies: StateFlow<List<StudyData>> = _myJoinedStudies

    private val _selectedStudyMembers = MutableStateFlow<List<AttendanceInfo>>(emptyList())
    val selectedStudyMembers: StateFlow<List<AttendanceInfo>> = _selectedStudyMembers

    private val _meetingsForStudy = MutableStateFlow<List<Meeting>>(emptyList())
    val meetingsForStudy: StateFlow<List<Meeting>> = _meetingsForStudy
    private val _pendingRequestCount = MutableStateFlow(0)
    val pendingRequestCount: StateFlow<Int> = _pendingRequestCount

    fun loadPendingRequestCount() {
        val ownerId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _pendingRequestCount.value = studyRepository.getPendingRequestCountForOwner(ownerId)
        }
    }
    fun loadMyStudies() {
        val currentUserId = UserRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                val userSnapshot = userRepository.getUserProfile(currentUserId).await()
                val userData = userSnapshot.toObject(UserData::class.java)

                userData?.let { user ->
                    if (user.createdStudyIds.isNotEmpty()) {
                        val created = studyRepository.getStudiesByIds(user.createdStudyIds)
                        _myCreatedStudies.value = created
                    }

                    if (user.joinedStudyIds.isNotEmpty()) {
                        val joined = studyRepository.getStudiesByIds(user.joinedStudyIds)
                        _myJoinedStudies.value = joined
                    }
                }
            } catch (e: Exception) {
                // TODO: 로그 출력 등 에러 처리
            }
        }
    }

    fun loadMeetingsForStudy(studyId: String) {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .whereEqualTo("parentStudyId", studyId)
                    .get()
                    .await()

                val meetings = snapshot.documents.mapNotNull { it.toObject(Meeting::class.java) }
                _meetingsForStudy.value = meetings
            } catch (e: Exception) {
                _meetingsForStudy.value = emptyList()
            }
        }
    }

    fun loadMembersForStudy(studyId: String) {
        viewModelScope.launch {
            try {
                val members = studyRepository.getAttendanceInfoForStudy(studyId)
                _selectedStudyMembers.value = members
            } catch (e: Exception) {
                _selectedStudyMembers.value = emptyList()
            }
        }
    }

    fun startSession(studyId: String, code: Int) {
        viewModelScope.launch {
            try {
                studyRepository.incrementTotalCountForStudy(studyId).await()
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }

    suspend fun getAttendanceCode(meetingId: String): Int? {
        val doc = FirebaseFirestore.getInstance()
            .collection("meetings")
            .document(meetingId)
            .get()
            .await()

        return doc.getLong("attendanceCode")?.toInt()
    }

    fun saveAttendanceCode(meetingId: String, code: Int) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("meetings")
                    .document(meetingId)
                    .update("attendanceCode", code)
                    .await()
            } catch (e: Exception) {
                // TODO: 예외 처리 (로그 출력 또는 UI 알림)
            }
        }
    }

    // [추가] 관리자 본인 출석 처리 함수
    fun markCurrentUserAsPresent(studyId: String, meetingId: String) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            try {
                // 🔍 사용자 정보 가져오기
                val userDoc = db.collection("users").document(currentUserId).get().await()
                val userName = userDoc.getString("name") ?: "이름없음"

                // 🔍 출석 횟수 정보 가져오기
                val memberInfo = _selectedStudyMembers.value.find { it.userId == currentUserId }

                val attendanceData = mapOf(
                    "userId" to currentUserId,
                    "name" to userName,
                    "studyName" to studyId,
                    "isPresent" to true,
                    "currentCount" to (memberInfo?.currentCount ?: 0) + 1,
                    "totalCount" to (memberInfo?.totalCount ?: 0),
                    "absentCount" to (memberInfo?.absentCount ?: 0)
                )

                // 🔧 미팅 기준 출석 기록
                db.collection("meetings")
                    .document(meetingId)
                    .collection("attendance")
                    .document(currentUserId)
                    .set(attendanceData, SetOptions.merge())
                    .await()

                // 🔧 스터디 기준 출석도 갱신
                studyRepository.markUserAsPresent(studyId, currentUserId).await()

                db.runTransaction { transaction ->
                    val memberRef = db.collection("studies")
                        .document(studyId)
                        .collection("members")
                        .document(currentUserId)

                    val snapshot = transaction.get(memberRef)
                    val current = snapshot.getLong("currentCount") ?: 0
                    val total = snapshot.getLong("totalCount") ?: 0

                    transaction.update(memberRef, mapOf(
                        "currentCount" to FieldValue.increment(1),
                        "totalCount" to FieldValue.increment(1),
                        "isPresent" to true
                    ))
                }.await()

                // 🔧 로컬 상태 업데이트
                _selectedStudyMembers.update { currentList ->
                    currentList.map { member ->
                        if (member.userId == currentUserId) {
                            member.copy(
                                isPresent = true,
                                currentCount = member.currentCount + 1
                            )
                        } else {
                            member
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Snackbar로 알림
                // TODO: 스낵바 등으로 알림
            }
        }
    }

    fun observeMembersForStudy(studyId: String) {
        val db = FirebaseFirestore.getInstance()
        val membersRef = db.collection("studies").document(studyId).collection("members")

        membersRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val memberList = snapshot.documents.map { doc ->
                val name = doc.getString("name") ?: ""
                val totalCount = doc.getLong("totalCount")?.toInt() ?: 0
                val currentCount = doc.getLong("currentCount")?.toInt() ?: 0
                val absentCount = totalCount - currentCount
                val isPresent = doc.getBoolean("isPresent") ?: false

                AttendanceInfo(
                    userId = doc.id,
                    name = name,
                    totalCount = totalCount,
                    currentCount = currentCount,
                    absentCount = absentCount,
                    isPresent = isPresent
                )
            }

            _selectedStudyMembers.value = memberList
        }
    }

    fun observeMeetingAttendance(meetingId: String) {
        FirebaseFirestore.getInstance()
            .collection("meetings")
            .document(meetingId)
            .collection("attendance")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val userId = doc.id
                    val isPresent = doc.getBoolean("isPresent") ?: false
                    val name = doc.getString("name") ?: ""
                    val studyName = doc.getString("studyName") ?: ""
                    val currentCount = (doc.getLong("currentCount") ?: 0L).toInt()
                    val totalCount = (doc.getLong("totalCount") ?: 0L).toInt()
                    val absentCount = (doc.getLong("absentCount") ?: 0L).toInt()

                    AttendanceInfo(
                        userId = userId,
                        isPresent = isPresent,
                        name = name,
                        studyName = studyName,
                        currentCount = currentCount,
                        totalCount = totalCount,
                        absentCount = absentCount
                    )
                } ?: emptyList()
                _meetingAttendance.value = list
            }
    }
    fun generateAttendanceCode(meetingId: String) {
        val randomCode = (1000..9999).random()
        FirebaseFirestore.getInstance()
            .collection("meetings")
            .document(meetingId)
            .update("attendanceCode", randomCode)
    }
    fun markAttendanceIfCodeMatches(
        studyId: String,
        meetingId: String,
        inputCode: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return

        FirebaseFirestore.getInstance()
            .collection("meetings")
            .document(meetingId)
            .get()
            .addOnSuccessListener { doc ->
                val serverCode = doc.get("attendanceCode")?.toString()
                if (serverCode == inputCode) {
                    val attendanceRef = doc.reference
                        .collection("attendance")
                        .document(currentUserId)

                    attendanceRef.set(mapOf("isPresent" to true), SetOptions.merge())
                        .addOnSuccessListener {
                            // ✅ 출석 성공 후 count 증가
                            markCurrentUserAsPresent(studyId, meetingId)

                            onResult(true)
                            observeMeetingAttendance(meetingId) // 카드 갱신
                        }
                } else {
                    onResult(false)
                }
            }
    }
}