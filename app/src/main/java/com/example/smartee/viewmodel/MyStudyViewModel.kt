// smartee/viewmodel/MyStudyViewModel.kt

package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.model.UserData
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import com.example.smartee.ui.attendance.AttendanceInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyStudyViewModel : ViewModel() {

    private val studyRepository = StudyRepository()
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())

    private val _myCreatedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myCreatedStudies: StateFlow<List<StudyData>> = _myCreatedStudies

    private val _myJoinedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myJoinedStudies: StateFlow<List<StudyData>> = _myJoinedStudies

    private val _selectedStudyMembers = MutableStateFlow<List<AttendanceInfo>>(emptyList())
    val selectedStudyMembers: StateFlow<List<AttendanceInfo>> = _selectedStudyMembers

    fun loadMyStudies() {
        val currentUserId = UserRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                val userSnapshot = userRepository.getUserProfile(currentUserId).await()
                val userData = userSnapshot.toObject(UserData::class.java)

                userData?.let { user ->
                    if (user.createdStudyIds.isNotEmpty()) {
                        val created = studyRepository.getStudiesByIds(user.createdStudyIds)
                        _myCreatedStudies.value = created
                    }

                    if (user.joinedStudyIds.isNotEmpty()) {
                        val joined = studyRepository.getStudiesByIds(user.joinedStudyIds)
                        _myJoinedStudies.value = joined
                    }
                }
            } catch (e: Exception) {
                // TODO: 로그 출력 등 에러 처리
            }
        }
    }

    fun loadMembersForStudy(studyId: String) {
        viewModelScope.launch {
            try {
                val members = studyRepository.getAttendanceInfoForStudy(studyId)
                _selectedStudyMembers.value = members
            } catch (e: Exception) {
                _selectedStudyMembers.value = emptyList()
            }
        }
    }

    fun startSession(studyId: String, code: Int) {
        viewModelScope.launch {
            studyRepository.createAttendanceSession(studyId, code)
        }
    }

    // [추가] 관리자 본인 출석 처리 함수
    fun markCurrentUserAsPresent(studyId: String) {
        val currentUserId = UserRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                // DB 업데이트
                studyRepository.markUserAsPresent(studyId, currentUserId).await()

                // 로컬 상태 즉시 업데이트
                _selectedStudyMembers.update { currentList ->
                    currentList.map { member ->
                        if (member.userId == currentUserId) {
                            member.copy(
                                isPresent = true,
                                currentCount = member.currentCount + 1
                            )
                        } else {
                            member
                        }
                    }
                }
            } catch (e: Exception) {
                // TODO: 스낵바 등으로 에러 알림
            }
        }
    }
}