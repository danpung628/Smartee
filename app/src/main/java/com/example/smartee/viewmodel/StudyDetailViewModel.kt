// smartee/viewmodel/StudyDetailViewModel.kt

package com.example.smartee.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.*
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class UserRole {
    GUEST, PARTICIPANT, OWNER
}

class StudyDetailViewModel : ViewModel() {
//    private val _isAttendanceSessionActive = MutableStateFlow(false)
//    val isAttendanceSessionActive = _isAttendanceSessionActive.asStateFlow()
//    private val _timeUntilNextMeeting = MutableStateFlow("")
//    val timeUntilNextMeeting = _timeUntilNextMeeting.asStateFlow()
//    private var isTimerRunning = false


    private val _activeMeetingSessions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val activeMeetingSessions = _activeMeetingSessions.asStateFlow()
    private var sessionCheckJob: Job? = null // [추가] 주기적인 체크 작업을 관리할 Job

    private val studyRepository = StudyRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())

    private val _studyData = MutableStateFlow<StudyData?>(null)
    val studyData = _studyData.asStateFlow()

    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings = _meetings.asStateFlow()

    private val _userRole = MutableStateFlow(UserRole.GUEST)
    val userRole = _userRole.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()


    private val _userEvent = MutableStateFlow<UserEvent?>(null)
    val userEvent = _userEvent.asStateFlow()

    // [추가] 모임별 '대기중'인 신청자 수를 저장하는 상태
    private val _pendingRequestCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val pendingRequestCounts = _pendingRequestCounts.asStateFlow()

    private val _participantStatusList = MutableStateFlow<List<ParticipantStatus>>(emptyList())
    val participantStatusList = _participantStatusList.asStateFlow()
    private var participantListener: ListenerRegistration? = null


    fun loadStudy(studyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val study = studyRepository.getStudyById(studyId)
                _studyData.value = study
                val currentUserId = UserRepository.getCurrentUserId()

                if (study != null && currentUserId != null) {
                    val role = when {
                        study.ownerId == currentUserId -> UserRole.OWNER
                        study.participantIds.contains(currentUserId) -> UserRole.PARTICIPANT
                        else -> UserRole.GUEST
                    }
                    _userRole.value = role

                    if (role == UserRole.OWNER || role == UserRole.PARTICIPANT) {
                        val fetchedMeetings = studyRepository.getMeetingsForStudy(studyId)
                        _meetings.value = fetchedMeetings
                        // [수정] 타이머 대신 새로운 세션 상태 체커를 시작
                        startSessionStatusChecker(fetchedMeetings)

                        if (role == UserRole.OWNER) {
                            val pendingRequests = studyRepository.getPendingMeetingRequestsForStudy(studyId)
                            _pendingRequestCounts.value = pendingRequests.groupingBy { it.meetingId }.eachCount()
                        }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestToJoinMeeting(meeting: Meeting) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        val study = _studyData.value ?: return

        viewModelScope.launch {
            try {
                val userSnapshot = userRepository.getUserProfile(currentUserId).await()
                val user = userSnapshot.toObject(UserData::class.java) ?: return@launch

                val request = MeetingJoinRequest(
                    meetingId = meeting.meetingId,
                    meetingTitle = meeting.title,
                    studyId = meeting.parentStudyId,
                    studyOwnerId = study.ownerId,
                    requesterId = currentUserId,
                    requesterNickname = user.nickname
                )
                studyRepository.createMeetingJoinRequest(request).await()
                _userEvent.value = UserEvent.RequestSentSuccessfully
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("모임 가입 신청 중 오류가 발생했습니다.")
            }
        }
    }



    fun deleteMeeting(meeting: Meeting) {
        viewModelScope.launch {
            studyRepository.deleteMeeting(meeting).addOnSuccessListener {
                loadStudy(meeting.parentStudyId)
            }
        }
    }
    fun joinMeeting(meeting: Meeting) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                studyRepository.addCurrentUserToMeeting(meeting.meetingId, currentUserId).await()
                loadStudy(meeting.parentStudyId)
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("모임 가입 중 오류가 발생했습니다.")
            }
        }
    }
    fun requestToJoinStudy() {
        if (_isLoading.value) return
        val study = _studyData.value ?: return
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val alreadyRequested = studyRepository.checkIfRequestExists(currentUserId, study.studyId)
                if (alreadyRequested) {
                    _userEvent.value = UserEvent.AlreadyRequested
                    return@launch
                }
                val userSnapshot = userRepository.getUserProfile(currentUserId).await()
                val user = userSnapshot.toObject(UserData::class.java) ?: return@launch
                if (user.ink >= study.minInkLevel && user.pen >= study.penCount) {
                    val request = JoinRequest(
                        studyId = study.studyId,
                        studyTitle = study.title,
                        requesterId = user.uid,
                        requesterNickname = user.nickname,
                        ownerId = study.ownerId
                    )
                    userRepository.updatePenCount(user.uid, user.pen - study.penCount)
                    studyRepository.createJoinRequest(request).await()
                    _userEvent.value = UserEvent.RequestSentSuccessfully
                } else {
                    _userEvent.value = UserEvent.JoinConditionsNotMet
                }
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("가입 신청 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun reportStudy(studyId: String) {
        Log.d("StudyDetailViewModel", "Reporting study: $studyId")
    }
    fun eventConsumed() {
        _userEvent.value = null
    }
    sealed class UserEvent {
        object RequestSentSuccessfully : UserEvent()
        object JoinConditionsNotMet : UserEvent()
        object AlreadyRequested : UserEvent()
        data class Error(val message: String) : UserEvent()
        object WithdrawSuccessful : UserEvent()

    }
    // [추가] 주기적으로 출석 세션 상태를 확인하는 함수
    private fun startSessionStatusChecker(meetings: List<Meeting>) {
        sessionCheckJob?.cancel()
        sessionCheckJob = viewModelScope.launch {
            while (true) {
                try {
                    val statusMap = meetings.map { meeting ->
                        // coroutineScope을 사용해 병렬로 세션 상태를 조회
                        async { meeting.meetingId to studyRepository.getActiveAttendanceSession(meeting.meetingId) }
                    }.awaitAll().toMap()
                    _activeMeetingSessions.value = statusMap
                } catch (e: Exception) {
                    // 오류 처리
                }
                delay(15000) // 15초마다 반복
            }
        }
    }
    fun withdrawFromMeeting(meetingId: String) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                studyRepository.withdrawFromMeeting(meetingId, currentUserId).await()
                // 참여 취소 후 화면의 모임 목록을 다시 로드하여 상태(참여 여부) 갱신
                _studyData.value?.let { loadStudy(it.studyId) }
                // [추가] 작업이 성공했음을 UI에 알립니다.
                _userEvent.value = UserEvent.WithdrawSuccessful
            } catch (e: Exception) {
                // 오류 처리
            }
        }
    }

    // [추가] 실시간으로 출석 현황을 구독하는 함수
    fun listenForParticipantStatus(meeting: Meeting) {
        participantListener?.remove() // 이전 리스너가 있다면 제거
        participantListener = studyRepository.listenForMeetingAttendance(meeting) { statuses ->
            _participantStatusList.value = statuses
        }
    }

    // [추가] 리스너를 제거하는 함수
    fun stopListeningForParticipantStatus() {
        participantListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        sessionCheckJob?.cancel() // ViewModel이 소멸될 때 작업 취소
    }
}