package com.example.feature_studylist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature_studylist.model.StudyData
import com.example.feature_studylist.model.StudyListFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel : ViewModel() {
    private val _studyList = StudyListFactory.makeStudyList()
    val studyList: MutableList<StudyData>
        get() = _studyList

    var searchKeyword by mutableStateOf("")
    var selectedAddress by mutableStateOf("")

    val filteredStudyList:MutableList<StudyData>
        get() = _studyList.filter {
            it.title.contains(searchKeyword) && it.address.contains(selectedAddress)
        }.toMutableList()

    //새로 고침 동작
    var isRefreshing by mutableStateOf(false)
        private set

    fun refreshStudyList() {
        // 강제로 약간의 지연을 줘야 애니메이션이 보임
        viewModelScope.launch {
            isRefreshing = true

            val newList = _studyList.map {
                it.copy()
            }
            _studyList.clear()
            _studyList.addAll(newList)

            delay(500) // 애니메이션만 보여주고 아무것도 안 함
            isRefreshing = false
        }
    }
}