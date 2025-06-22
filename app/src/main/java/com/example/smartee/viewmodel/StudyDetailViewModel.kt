// smartee/viewmodel/StudyDetailViewModel.kt

package com.example.smartee.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.*
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
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

    private val _timeUntilNextMeeting = MutableStateFlow("")
    val timeUntilNextMeeting = _timeUntilNextMeeting.asStateFlow()

    private var isTimerRunning = false

    private val _userEvent = MutableStateFlow<UserEvent?>(null)
    val userEvent = _userEvent.asStateFlow()

    // [추가] 모임별 '대기중'인 신청자 수를 저장하는 상태
    private val _pendingRequestCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val pendingRequestCounts = _pendingRequestCounts.asStateFlow()

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
                        findNextMeetingAndStartTimer(fetchedMeetings, currentUserId)

                        // [추가] 관리자일 경우, 모임별 신청자 수 계산
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


    private fun findNextMeetingAndStartTimer(meetings: List<Meeting>, currentUserId: String) {
        if (isTimerRunning) return

        val now = LocalDateTime.now()
        val joinedMeetings = meetings.filter { it.confirmedParticipants.contains(currentUserId) }

        if (joinedMeetings.isEmpty()) {
            _timeUntilNextMeeting.value = "출석할 모임이 없습니다"
            return
        }

        val nextMeetingTime = joinedMeetings.mapNotNull {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val meetingTimeStr = "${it.date} ${it.time.substringBefore('~')}"
                LocalDateTime.parse(meetingTimeStr, formatter)
            } catch (e: Exception) { null }
        }.filter { it.isAfter(now) }.minOrNull()

        if (nextMeetingTime != null) {
            startCountdownTimer(nextMeetingTime)
        } else {
            _timeUntilNextMeeting.value = "참여한 모임이 모두 종료되었습니다"
        }
    }

    private fun startCountdownTimer(meetingTime: LocalDateTime) {
        isTimerRunning = true
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val totalSeconds = ChronoUnit.SECONDS.between(now, meetingTime)

                if (totalSeconds <= 0) {
                    _timeUntilNextMeeting.value = "출석 가능"
                    break
                }

                if (totalSeconds < 60) {
                    _timeUntilNextMeeting.value = "잠시후 스터디 출석이 시작됩니다."
                    delay(1000)
                    continue
                }

                val days = totalSeconds / (24 * 3600)
                val hours = (totalSeconds % (24 * 3600)) / 3600
                val minutes = (totalSeconds % 3600) / 60

                val builder = StringBuilder()
                if (days > 0) builder.append("${days}일 ")
                if (hours > 0) builder.append("${hours}시간 ")
                builder.append("${minutes}분 후 시작")

                _timeUntilNextMeeting.value = builder.toString()
                delay(1000)
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
    }
}