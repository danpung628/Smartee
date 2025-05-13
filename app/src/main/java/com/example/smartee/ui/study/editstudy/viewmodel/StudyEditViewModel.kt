package com.example.smartee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.smartee.model.StudyData
import com.google.firebase.Timestamp

class StudyEditViewModel : ViewModel() {
    var studyId by mutableStateOf("")
    var title by mutableStateOf("")
    var category by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    var isRegular by mutableStateOf(false)
    var maxMemberCount by mutableStateOf("")
    var isOffline by mutableStateOf(true)
    var minInkLevel by mutableStateOf("")
    var penCount by mutableStateOf("")
    var punishment by mutableStateOf("")
    var description by mutableStateOf("")
    var address by mutableStateOf("")
    var thumbnailModel by mutableStateOf("")

    fun loadStudyData(data: StudyData) {
        studyId = data.studyId
        title = data.title
        category = data.category
        startDate = data.startDate
        endDate = data.endDate
        isRegular = data.isRegular
        maxMemberCount = data.maxMemberCount.toString()
        isOffline = data.isOffline
        minInkLevel = data.minInkLevel.toString()
        penCount = data.penCount.toString()
        punishment = data.punishment
        description = data.description
        address = data.address
        thumbnailModel = data.thumbnailModel
    }

    fun toStudyData(): StudyData {
        return StudyData(
            studyId = studyId, // 기존 ID 유지 (수정 시)
            title = title,
            category = category,
            dateTimestamp = Timestamp.now(), // 현재 시간으로 설정
            startDate = startDate,
            endDate = endDate,
            isRegular = isRegular,
            currentMemberCount = 0, // 새 스터디는 현재 멤버 0명으로 시작
            maxMemberCount = maxMemberCount.toIntOrNull() ?: 0,
            isOffline = isOffline,
            minInkLevel = minInkLevel.toIntOrNull() ?: 0,
            penCount = penCount.toIntOrNull() ?: 0,
            punishment = punishment,
            description = description,
            address = address,
            thumbnailModel = thumbnailModel
        )
    }
}