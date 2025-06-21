package com.example.smartee.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.JoinRequest
import com.example.smartee.model.StudyData
import com.example.smartee.model.UserData
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudyDetailViewModel : ViewModel() {
    private val studyRepository = StudyRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())

    private val _studyData = MutableStateFlow<StudyData?>(null)
    val studyData = _studyData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _userEvent = MutableStateFlow<UserEvent?>(null)
    val userEvent = _userEvent.asStateFlow()

    fun loadStudy(studyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _studyData.value = studyRepository.getStudyById(studyId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestToJoinStudy() {
        val study = _studyData.value ?: return
        val currentUserId = UserRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
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