package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.repository.StudyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyStudyViewModel : ViewModel() {

    private val studyRepository = StudyRepository()

    private val _myCreatedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myCreatedStudies: StateFlow<List<StudyData>> = _myCreatedStudies

    private val _myJoinedStudies = MutableStateFlow<List<StudyData>>(emptyList())
    val myJoinedStudies: StateFlow<List<StudyData>> = _myJoinedStudies

    fun loadMyStudies() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return  // ✅ 이메일 기준으로 변경

        viewModelScope.launch {
            val allStudies = studyRepository.getAllStudies()

            val created = allStudies.filter { it.managerId == currentUserEmail }  // ✅ 이메일 비교
            val joined = allStudies.filter {
                it.participantIds.contains(currentUserEmail) && it.managerId != currentUserEmail
            }

            _myCreatedStudies.value = created
            _myJoinedStudies.value = joined
        }
    }
}
