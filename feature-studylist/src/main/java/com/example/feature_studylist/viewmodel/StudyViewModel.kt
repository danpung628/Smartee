package com.example.feature_studylist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.feature_studylist.model.StudyData

class StudyViewModel : ViewModel() {
    private val _studyList = mutableStateListOf(
        StudyData(
            "영어 스터디",
            address = "군자동",
            maxMemberCount = 6
        ),
        StudyData(
            "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6
        ),
        StudyData(
            "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE
        )
    )
    val studyList: MutableList<StudyData>
        get() = _studyList
}