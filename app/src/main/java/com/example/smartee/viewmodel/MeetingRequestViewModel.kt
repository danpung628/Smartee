// smartee/viewmodel/MeetingRequestViewModel.kt

package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.MeetingJoinRequest
import com.example.smartee.repository.StudyRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MeetingRequestViewModel : ViewModel() {
    private val studyRepository = StudyRepository()

    private val _requests = MutableStateFlow<List<MeetingJoinRequest>>(emptyList())
    val requests = _requests.asStateFlow()

    // [추가] UI 이벤트를 위한 SharedFlow
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun loadRequests(meetingId: String) {
        viewModelScope.launch {
            try {
                _requests.value = studyRepository.getPendingRequestsForMeeting(meetingId)
            } catch (e: Exception) {
                // [수정] 에러 발생 시 이벤트 전송
                _uiEvent.emit(UiEvent.ShowSnackbar("신청 목록을 불러오는 중 오류가 발생했습니다."))
            }
        }
    }

    fun approveRequest(request: MeetingJoinRequest) {
        viewModelScope.launch {
            try {
                studyRepository.approveMeetingJoinRequest(request).await()
                loadRequests(request.meetingId)
            } catch (e: Exception) {
                // [수정] 에러 발생 시 이벤트 전송
                _uiEvent.emit(UiEvent.ShowSnackbar("승인 처리 중 오류가 발생했습니다."))
            }
        }
    }

    fun rejectRequest(request: MeetingJoinRequest) {
        viewModelScope.launch {
            try {
                studyRepository.rejectMeetingJoinRequest(request.requestId).await()
                loadRequests(request.meetingId)
            } catch (e: Exception) {
                // [수정] 에러 발생 시 이벤트 전송
                _uiEvent.emit(UiEvent.ShowSnackbar("거절 처리 중 오류가 발생했습니다."))
            }
        }
    }

    // [추가] UI 이벤트를 위한 Sealed Class
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}