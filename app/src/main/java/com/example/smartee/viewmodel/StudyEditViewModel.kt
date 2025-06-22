package com.example.smartee.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch
import java.time.LocalDate

class StudyEditViewModel : ViewModel() {
    var studyId by mutableStateOf("")
    var title by mutableStateOf("")
    var category by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var isRegular by mutableStateOf(false)
    var maxMemberCount by mutableStateOf("")
    var isOffline by mutableStateOf(true)
    var minInkLevel by mutableStateOf("")
    var penCount by mutableStateOf("")
    var punishment by mutableStateOf("")
    var description by mutableStateOf("")
    var address by mutableStateOf("")
    var thumbnailModel by mutableStateOf("")
    var selectedCategories = mutableStateListOf<String>()
    private val db = FirebaseFirestore.getInstance()
    fun loadStudyFromFirebase(studyId: String) {
        this.studyId = studyId
        viewModelScope.launch {
            db.collection("studies").document(studyId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val study = document.toObject<StudyData>()?.copy(studyId = document.id)
                        if (study != null) {
                            loadStudyData(study)
                            Log.d("StudyEditViewModel", "✅ 스터디 불러오기 성공: ${study.title}")
                        } else {
                            Log.w("StudyEditViewModel", "⚠️ 스터디 데이터 파싱 실패")
                        }
                    } else {
                        Log.w("StudyEditViewModel", "⚠️ 문서가 존재하지 않음")
                    }
                }
                .addOnFailureListener {
                    Log.e("StudyEditViewModel", "❌ Firestore 불러오기 실패", it)
                }
        }
    }


    fun loadStudyData(data: StudyData) {
        studyId = data.studyId
        title = data.title
        category = data.category
        startDate = parseLocalDate(data.startDate)
        endDate = parseLocalDate(data.endDate)
        isRegular = data.isRegular
        maxMemberCount = data.maxMemberCount.toString()
        isOffline = data.isOffline
        minInkLevel = data.minInkLevel.toString()
        penCount = data.penCount.toString()
        punishment = data.punishment
        description = data.description
        address = data.address
        thumbnailModel = data.thumbnailModel
        selectedCategories.clear()
        if (data.category.isNotEmpty()) {
            selectedCategories.addAll(data.category.split(","))
        }
    }


    fun toStudyData(): StudyData {
        return StudyData(
            studyId = studyId, // 기존 ID 유지 (수정 시)
            title = title,
            category = selectedCategories.joinToString(","),
            dateTimestamp = Timestamp.now(), // 현재 시간으로 설정
            startDate = startDate?.toString() ?: "",  // LocalDate를 String으로 변환
            endDate = endDate?.toString() ?: "",      // LocalDate를 String으로 변환
            isRegular = isRegular,
            //currentMemberCount = 0, // 새 스터디는 현재 멤버 0명으로 시작
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
    private fun parseLocalDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    // 카테고리 토글 메소드 추가
    fun toggleCategory(category: String) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            if (selectedCategories.size < 5) {  // 최대 5개 제한
                selectedCategories.add(category)
            }
        }
    }
}
