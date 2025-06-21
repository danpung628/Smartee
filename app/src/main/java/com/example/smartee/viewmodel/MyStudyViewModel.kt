package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.model.UserData
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyStudyViewModel : ViewModel() {

    private val studyRepository = StudyRepository()
    // ✅ UserRepository 인스턴스 추가
    private val userRepository = UserRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance())


    private val _myCreatedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myCreatedStudies: StateFlow<List<StudyData>> = _myCreatedStudies

    private val _myJoinedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myJoinedStudies: StateFlow<List<StudyData>> = _myJoinedStudies

    fun loadMyStudies() {
        val currentUserId = UserRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                // 1. 현재 사용자의 문서를 가져옵니다.
                val userSnapshot = userRepository.getUserProfile(currentUserId).await()
                val userData = userSnapshot.toObject(UserData::class.java)

                userData?.let { user ->
                    // 2. 사용자의 createdStudyIds 리스트로 내가 만든 스터디를 조회합니다.
                    if (user.createdStudyIds.isNotEmpty()) {
                        val created = studyRepository.getStudiesByIds(user.createdStudyIds)
                        _myCreatedStudies.value = created
                    }

                    // 3. 사용자의 joinedStudyIds 리스트로 내가 참여한 스터디를 조회합니다.
                    if (user.joinedStudyIds.isNotEmpty()) {
                        val joined = studyRepository.getStudiesByIds(user.joinedStudyIds)
                        _myJoinedStudies.value = joined
                    }
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}