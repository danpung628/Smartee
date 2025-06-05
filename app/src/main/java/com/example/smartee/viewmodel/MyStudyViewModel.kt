package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
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
        val currentUserId = UserRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val allStudies = studyRepository.getAllStudies()
            val created = allStudies.filter { it.managerId == currentUserId }
            val joined = allStudies.filter {
                it.participantIds.contains(currentUserId) && it.managerId != currentUserId
            }

            _myCreatedStudies.value = created
            _myJoinedStudies.value = joined
        }
    }
}
