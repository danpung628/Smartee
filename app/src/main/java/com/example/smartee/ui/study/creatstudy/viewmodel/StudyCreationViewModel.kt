package com.example.smartee.ui.study.creatstudy.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.smartee.model.StudyCreationData
import java.time.LocalDate


class StudyCreationViewModel : ViewModel() {
    var name by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var maxParticipants by mutableStateOf("")
    var isOffline by mutableStateOf(false)
    var minInk by mutableStateOf("")
    var isRegular by mutableStateOf(false)
    var selectedCategories = mutableStateListOf<String>()
    var penCount by mutableStateOf("")
    var punishment by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)

    // 누적 저장 리스트
    var submittedStudies = mutableStateListOf<StudyCreationData>()
        private set

    fun validate(): Boolean {
        return name.isNotBlank() &&
                startDate != null &&
                endDate != null &&
                maxParticipants.toIntOrNull() != null &&
                minInk.toIntOrNull() != null &&
                selectedCategories.isNotEmpty() &&
                penCount.toIntOrNull() != null
    }

    fun submit() {
        if (!validate()) {
            errorMessage = "입력값을 확인해주세요."
            return
        }
        errorMessage = null

        val newStudy = StudyCreationData(
            name,
            startDate,
            endDate,
            maxParticipants.toInt(),
            isOffline,
            minInk.toInt(),
            isRegular,
            selectedCategories.toList(),
            penCount.toInt(),
            if (punishment.isBlank()) null else punishment
        )

        submittedStudies.add(newStudy)
        //디버깅용
        Log.d("StudyDebug", "===== 현재까지 생성된 스터디 목록 =====")
        submittedStudies.forEachIndexed { index, study ->
            Log.d("StudyDebug", "${index + 1}. 이름: ${study.name}, 카테고리: ${study.category}")
        }
        // 초기화
        name = ""
        startDate = null
        endDate = null
        maxParticipants = ""
        isOffline = false
        minInk = ""
        isRegular = false
        selectedCategories.clear()
        penCount = ""
        punishment = ""
    }
    fun toggleCategory(category: String) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            if (selectedCategories.size < 5) {
                selectedCategories.add(category)
            }
        }
    }

}