package com.example.smartee.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
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
    fun loadStudyFromFirebase(studyId: String, onComplete: () -> Unit) {
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
                // [추가] 작업이 성공하든 실패하든 항상 onComplete 콜백을 호출하여
                // 화면에 로딩이 끝났음을 알려줍니다.
                .addOnCompleteListener {
                    onComplete()
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
            // [수정] 각 카테고리 문자열의 앞뒤 공백을 제거합니다.
            selectedCategories.addAll(data.category.split(",").map { it.trim() })
        }
    }


    // [수정] 이 함수를 수정하여 업데이트할 데이터만 Map으로 반환하도록 합니다.
    fun toStudyData(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "category" to selectedCategories.joinToString(","),
            "startDate" to (startDate?.toString() ?: ""),
            "endDate" to (endDate?.toString() ?: ""),
            "isRegular" to isRegular,
            "maxMemberCount" to (maxMemberCount.toIntOrNull() ?: 0),
            "isOffline" to isOffline,
            "minInkLevel" to (minInkLevel.toIntOrNull() ?: 0),
            "penCount" to (penCount.toIntOrNull() ?: 0),
            "punishment" to punishment,
            "description" to description,
            "address" to address,
            "thumbnailModel" to thumbnailModel
            // ownerId, participantIds 등 기존 정보는 여기에 포함하지 않습니다.
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
