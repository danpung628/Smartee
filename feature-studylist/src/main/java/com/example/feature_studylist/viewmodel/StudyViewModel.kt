package com.example.feature_studylist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature_studylist.model.StudyData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel : ViewModel() {
    private val _studyList = mutableStateListOf(
        StudyData(
            studyId = "0",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11
        ),
        StudyData(
            studyId = "1",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2
        ),
        StudyData(
            studyId = "2",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE
        ),
        StudyData(
            studyId = "0",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11
        ),
        StudyData(
            studyId = "1",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2
        ),
        StudyData(
            studyId = "2",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE
        ),
        StudyData(
            studyId = "0",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11
        ),
        StudyData(
            studyId = "1",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2
        ),
        StudyData(
            studyId = "2",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE
        ),
        StudyData(
            studyId = "0",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11
        ),
        StudyData(
            studyId = "1",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2
        ),
        StudyData(
            studyId = "2",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE
        ),
    )
    val studyList: MutableList<StudyData>
        get() = _studyList

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