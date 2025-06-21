package com.example.smartee.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.JoinRequest
import com.example.smartee.model.Meeting
import com.example.smartee.model.StudyData
import com.example.smartee.model.UserData
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
                        findNextMeetingAndStartTimer(fetchedMeetings)
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun findNextMeetingAndStartTimer(meetings: List<Meeting>) {
        if (isTimerRunning) return

        val now = LocalDateTime.now()
        val nextMeeting = meetings.mapNotNull {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val meetingTimeStr = "${it.date} ${it.time.substringBefore('~')}"
                LocalDateTime.parse(meetingTimeStr, formatter)
            } catch (e: Exception) {
                null
            }
        }.filter { it.isAfter(now) }.minOrNull()

        nextMeeting?.let { startCountdownTimer(it) }
    }

    private fun startCountdownTimer(meetingTime: LocalDateTime) {
        isTimerRunning = true
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val remainingSeconds = ChronoUnit.SECONDS.between(now, meetingTime)

                if (remainingSeconds <= 0) {
                    _timeUntilNextMeeting.value = "출석 가능"
                    break
                }

                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                _timeUntilNextMeeting.value = String.format("%02d:%02d", minutes, seconds)
                delay(1000)
            }
        }
    }

    fun requestToJoinStudy() {
        if (_isLoading.value) return // 중복 요청 방지

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
        // TODO: 스터디 신고 로직 구현
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