// smartee/viewmodel/StudyDetailViewModel.kt

package com.example.smartee.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.smartee.bluetooth.BluetoothClientService
import com.example.smartee.model.*
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class UserRole {
    GUEST, PARTICIPANT, OWNER
}

class StudyDetailViewModel(application: Application) : AndroidViewModel(application) {
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

    private val _activeMeetingSessions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val activeMeetingSessions = _activeMeetingSessions.asStateFlow()
    private var sessionCheckJob: Job? = null

    private val _participantStatusList = MutableStateFlow<List<ParticipantStatus>>(emptyList())
    val participantStatusList = _participantStatusList.asStateFlow()
    private var participantListener: ListenerRegistration? = null

    // [추가] 생성된 출석 코드를 저장할 상태 변수
    private val _generatedAttendanceCode = MutableStateFlow<Int?>(null)
    val generatedAttendanceCode = _generatedAttendanceCode.asStateFlow()
    private val _pendingRequestCount = MutableStateFlow(0)
    val pendingRequestCount = _pendingRequestCount.asStateFlow()

    // [추가] 알림 개수를 불러오는 함수
    fun loadPendingRequestCount() {
        val ownerId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            // 자신의 스터디일 때만 요청 개수를 가져옴
            if (_userRole.value == UserRole.OWNER) {
                _pendingRequestCount.value = studyRepository.getPendingRequestCountForOwner(ownerId)
            }
        }
    }
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
                        startSessionStatusChecker(fetchedMeetings)
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // [추가] 관리자가 출석 세션을 시작하는 함수
    fun startAttendanceSession(meetingId: String) {
        viewModelScope.launch {
            val randomCode = (1000..9999).random()
            try {
                studyRepository.createAttendanceSession(meetingId, randomCode).await()
                _generatedAttendanceCode.value = randomCode
                startSessionStatusChecker(meetings.value)
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("출석 세션 시작에 실패했습니다.")
            }
        }
    }

    // [추가] 관리자 본인 출석 처리 함수
    fun markHostAsPresent(meetingId: String) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        val study = _studyData.value ?: return
        val user = study.participantIds.find { it == currentUserId }

        viewModelScope.launch {
            try {
                // [수정] Repository 함수에 필요한 모든 정보를 전달합니다.
                studyRepository.markAttendance(
                    meetingId = meetingId,
                    userId = currentUserId,
                    parentStudyId = study.studyId,
                    userName = study.ownerNickname, // UserData에서 닉네임 가져오기
                    studyName = study.title
                ).await()

                // 본인 출석 후 출석 현황을 즉시 갱신
                _meetings.value.find { it.meetingId == meetingId }?.let {
                    listenForParticipantStatus(it)
                }
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("본인 출석 처리에 실패했습니다.")
            }
        }
    }

    fun requestToJoinMeeting(meeting: Meeting) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userSnapshot = userRepository.getUserProfile(currentUserId).await()
                val user = userSnapshot.toObject(UserData::class.java) ?: return@launch

                val request = MeetingJoinRequest(
                    meetingId = meeting.meetingId,
                    meetingTitle = meeting.title,
                    studyId = meeting.parentStudyId,
                    studyOwnerId = _studyData.value?.ownerId ?: "",
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

                // [추가] 현재 사용자의 프로필 정보를 가져와서 가입 조건 확인
                val user = userRepository.getUserProfile(currentUserId).await().toObject(UserData::class.java)
                if (user == null) {
                    _userEvent.value = UserEvent.Error("사용자 정보를 가져올 수 없습니다.")
                    return@launch
                }

                // [추가] 잉크 및 만년필 조건 확인
                if (user.ink < study.minInkLevel) {
                    _userEvent.value = UserEvent.Error("참여에 필요한 잉크(${study.minInkLevel})가 부족합니다.")
                    return@launch
                }
                if (user.pen < study.penCount) {
                    _userEvent.value = UserEvent.Error("참여에 필요한 만년필(${study.penCount}개)이 부족합니다.")
                    return@launch
                }

                // [추가] JoinRequest 객체 생성
                val request = JoinRequest(
                    studyId = study.studyId,
                    studyTitle = study.title,
                    requesterId = user.uid,
                    requesterNickname = user.nickname,
                    ownerId = study.ownerId,
                    timestamp = com.google.firebase.Timestamp.now()
                )

                // [추가] Firestore에 요청 문서 생성
                studyRepository.createJoinRequest(request).await()

                _userEvent.value = UserEvent.RequestSentSuccessfully
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("가입 신청 중 오류가 발생했습니다: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reportStudy(studyId: String) {
        // 신고 로직
    }

    fun eventConsumed() {
        _userEvent.value = null
    }

    private fun startSessionStatusChecker(meetings: List<Meeting>) {
        sessionCheckJob?.cancel()
        sessionCheckJob = viewModelScope.launch {
            while (true) {
                try {
                    val statusMap = meetings.map { meeting ->
                        async { meeting.meetingId to studyRepository.getActiveAttendanceSession(meeting.meetingId) }
                    }.awaitAll().toMap()
                    _activeMeetingSessions.value = statusMap
                } catch (e: Exception) {
                    // 오류 처리
                }
                delay(15000)
            }
        }
    }

    fun withdrawFromMeeting(meetingId: String) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                studyRepository.withdrawFromMeeting(meetingId, currentUserId).await()
                _studyData.value?.let { loadStudy(it.studyId) }
                _userEvent.value = UserEvent.WithdrawSuccessful
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("참여 취소 중 오류가 발생했습니다.")
            }
        }
    }

    fun listenForParticipantStatus(meeting: Meeting) {
        participantListener?.remove()
        participantListener = studyRepository.listenForMeetingAttendance(meeting) { statuses ->
            _participantStatusList.value = statuses
        }
    }

    fun stopListeningForParticipantStatus() {
        participantListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        sessionCheckJob?.cancel()
        stopListeningForParticipantStatus()
    }
    fun performBluetoothAttendance(meeting: Meeting) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                // [추가] 블루투스 출석을 시도하고 성공/실패 여부를 UI에 알립니다.
                val bluetoothClient = BluetoothClientService(getApplication())
                bluetoothClient.sendAttendance(
                    studyId = meeting.parentStudyId,
                    meetingId = meeting.meetingId,
                    userId = currentUserId
                )
                _userEvent.value = UserEvent.ShowSnackbar("블루투스 출석을 시도했습니다. 잠시 후 새로고침하여 확인해주세요.")
            } catch(e: Exception) {
                _userEvent.value = UserEvent.Error("블루투스 출석 중 오류 발생: ${e.message}")
            }
        }
    }
    sealed class UserEvent {
        object RequestSentSuccessfully : UserEvent()
        object JoinConditionsNotMet : UserEvent()
        object AlreadyRequested : UserEvent()
        data class Error(val message: String) : UserEvent()
        object WithdrawSuccessful : UserEvent()
        data class ShowSnackbar(val message: String): UserEvent()

    }
}