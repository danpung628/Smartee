
package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.Meeting
import com.example.smartee.model.ParticipantStatus
import com.example.smartee.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeetingStatusViewModel(
    private val meeting: Meeting,
    private val studyRepository: StudyRepository = StudyRepository()
) : ViewModel() {

    private val _participantStatusList = MutableStateFlow<List<ParticipantStatus>>(emptyList())
    val participantStatusList = _participantStatusList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadMeetingStatus()
    }

    private fun loadMeetingStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Repository에 새로 만들 함수를 호출합니다.
                _participantStatusList.value = studyRepository.getMeetingAttendanceStatus(meeting)
            } catch (e: Exception) {
                // TODO: 에러 처리
            } finally {
                _isLoading.value = false
            }
        }
    }
}