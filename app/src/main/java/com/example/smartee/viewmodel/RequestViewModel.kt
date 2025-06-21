package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.JoinRequest
import com.example.smartee.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestViewModel : ViewModel() {
    private val studyRepository = StudyRepository()

    private val _requests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val requests = _requests.asStateFlow()

    fun loadRequests(studyId: String) {
        viewModelScope.launch {
            _requests.value = studyRepository.getPendingRequestsForStudy(studyId)
        }
    }

    fun approveRequest(request: JoinRequest, studyId: String) {
        viewModelScope.launch {
            studyRepository.approveJoinRequest(request).addOnSuccessListener {
                loadRequests(studyId)
            }
        }
    }

    fun rejectRequest(request: JoinRequest, studyId: String) {
        viewModelScope.launch {
            studyRepository.rejectJoinRequest(request.requestId).addOnSuccessListener {
                loadRequests(studyId)
            }
        }
    }
}