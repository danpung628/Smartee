// smartee/viewmodel/StudyDetailViewModel.kt

package com.example.smartee.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartee.bluetooth.AttendanceResult
import com.example.smartee.bluetooth.BluetoothClientService
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

enum class UserRole {
    GUEST, PARTICIPANT, OWNER
}

// [수정] ViewModel을 AndroidViewModel로 변경하여 Application Context에 접근 가능하게 합니다.
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

    private val _generatedAttendanceCode = MutableStateFlow<Int?>(null)
    val generatedAttendanceCode = _generatedAttendanceCode.asStateFlow()

    private val _pendingRequestCount = MutableStateFlow(0)
    val pendingRequestCount = _pendingRequestCount.asStateFlow()
    private val _isConnectingBluetooth = MutableStateFlow(false)

    val isConnectingBluetooth = _isConnectingBluetooth.asStateFlow()
    private val bluetoothClientService = BluetoothClientService(application)

    val isScanning = bluetoothClientService.isScanning
    val discoveredDevices = bluetoothClientService.discoveredDevices

    fun loadPendingRequestCount() {
        val ownerId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
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

    fun markHostAsPresent(meetingId: String) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        val study = _studyData.value ?: return
        viewModelScope.launch {
            try {
                studyRepository.markAttendance(
                    meetingId = meetingId,
                    userId = currentUserId,
                    parentStudyId = study.studyId,
                    userName = study.ownerNickname,
                    studyName = study.title
                ).await()

                _meetings.value.find { it.meetingId == meetingId }?.let {
                    listenForParticipantStatus(it)
                }
            } catch (e: Exception) {
                _userEvent.value = UserEvent.Error("본인 출석 처리에 실패했습니다.")
            }
        }
    }
    fun startDeviceScan() {
        bluetoothClientService.startDiscovery()
    }

    fun stopDeviceScan() {
        bluetoothClientService.stopDiscovery()
    }
    // [추가] 블루투스 출석을 처리하는 함수
    fun performBluetoothAttendance(device: BluetoothDevice, meeting: Meeting) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isConnectingBluetooth.value = true
            try {
                val result = bluetoothClientService.sendAttendance(
                    device = device,
                    studyId = meeting.parentStudyId,
                    meetingId = meeting.meetingId,
                    userId = currentUserId
                )

                when (result) {
                    is AttendanceResult.Success -> {
                        _userEvent.value = UserEvent.ShowSnackbar("출석 정보 전송 성공! 잠시 후 반영됩니다.")
                    }
                    is AttendanceResult.Failure -> {
                        _userEvent.value = UserEvent.Error(result.reason)
                    }
                }
            } catch(e: Exception) {
                _userEvent.value = UserEvent.Error("블루투스 출석 중 알 수 없는 오류 발생: ${e.message}")
            } finally {
                _isConnectingBluetooth.value = false
            }
        }
    }

    fun requestToJoinMeeting(meeting: Meeting) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val user = userRepository.getUserProfile(currentUserId).await().toObject(UserData::class.java)
                if (user == null) {
                    _userEvent.value = UserEvent.Error("사용자 정보를 찾을 수 없습니다.")
                    return@launch
                }
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
                val user = userRepository.getUserProfile(currentUserId).await().toObject(UserData::class.java)
                if (user == null) {
                    _userEvent.value = UserEvent.Error("사용자 정보를 찾을 수 없습니다.")
                    return@launch
                }
                if (user.ink < study.minInkLevel) {
                    _userEvent.value = UserEvent.Error("참여에 필요한 잉크(${study.minInkLevel})가 부족합니다.")
                    return@launch
                }
                if (user.pen < study.penCount) {
                    _userEvent.value = UserEvent.Error("참여에 필요한 만년필(${study.penCount}개)이 부족합니다.")
                    return@launch
                }
                val request = JoinRequest(
                    studyId = study.studyId,
                    studyTitle = study.title,
                    requesterId = user.uid,
                    requesterNickname = user.nickname,
                    ownerId = study.ownerId,
                    timestamp = com.google.firebase.Timestamp.now()
                )
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
                } catch (e: Exception) { /* 오류 처리 */ }
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
        stopDeviceScan() // [추가] ViewModel 소멸 시 검색 중지
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

// [추가] ViewModel에 Application을 주입하기 위한 Factory 클래스
class StudyDetailViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}