// smartee/viewmodel/RequestViewModel.kt

package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.JoinRequest
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RequestViewModel : ViewModel() {
    private val studyRepository = StudyRepository()
    private val _requests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val requests: StateFlow<List<JoinRequest>> = _requests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // [수정] studyId 대신 현재 로그인한 사용자의 모든 요청을 불러옵니다.
    fun loadRequestsForCurrentUser() {
        val ownerId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _requests.value = studyRepository.getRequestsForOwner(ownerId)
            _isLoading.value = false
        }
    }

    fun approveRequest(request: JoinRequest) {
        viewModelScope.launch {
            studyRepository.approveJoinRequest(request).addOnCompleteListener {
                loadRequestsForCurrentUser() // 목록 새로고침
            }
        }
    }

    fun rejectRequest(request: JoinRequest) {
        viewModelScope.launch {
            studyRepository.rejectJoinRequest(request.requestId).addOnCompleteListener {
                loadRequestsForCurrentUser() // 목록 새로고침
            }
        }
    }
}