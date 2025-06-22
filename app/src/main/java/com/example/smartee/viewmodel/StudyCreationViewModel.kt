// smartee/viewmodel/StudyCreationViewModel.kt
package com.example.smartee.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.model.UserData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class StudyCreationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var title by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var maxParticipants by mutableStateOf("")
    var isOffline by mutableStateOf(false)
    var minInk by mutableStateOf("")
    var isRegular by mutableStateOf(false)
    var selectedCategories = mutableStateListOf<String>()
    var penCount by mutableStateOf("")
    var punishment by mutableStateOf("")
    var description by mutableStateOf("반갑습니다 ㅎㅎ")
    var address by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var submittedStudies = mutableStateListOf<StudyData>()
        private set

    fun validate(): Boolean {
        // ... (기존 validate 함수와 동일)
        return title.isNotBlank() &&
                startDate != null &&
                endDate != null &&
                maxParticipants.toIntOrNull() != null &&
                minInk.toIntOrNull() != null &&
                selectedCategories.isNotEmpty() &&
                penCount.toIntOrNull() != null
    }

    fun submit() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            errorMessage = "로그인이 필요합니다."
            return
        }
        if (!validate()) {
            errorMessage = "입력값을 확인해주세요."
            return
        }
        errorMessage = null

        val newStudy = StudyData(
            // ... (기존 StudyData 객체 생성 로직과 동일)
            ownerId = currentUser.uid,
            participantIds = listOf(currentUser.uid),
            title = title,
            category = selectedCategories.joinToString(","),
            dateTimestamp = Timestamp.now(),
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            isRegular = isRegular,
            maxMemberCount = maxParticipants.toIntOrNull() ?: 0,
            isOffline = isOffline,
            minInkLevel = minInk.toIntOrNull() ?: 0,
            penCount = penCount.toIntOrNull() ?: 0,
            punishment = punishment,
            description = description,
            address = address,
            commentCount = 0,
            likeCount = 0,
            thumbnailModel = "https://picsum.photos/300/200"
        )
        addStudyToFirebase(newStudy)
    }

    private fun addStudyToFirebase(study: StudyData) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val userRef = db.collection("users").document(currentUser.uid)
                val newStudyRef = db.collection("studies").document()

                // Firestore Transaction을 사용하여 여러 작업을 원자적으로 처리
                db.runTransaction { transaction ->
                    val userDoc = transaction.get(userRef)

                    // 1. 스터디 생성 카운트 업데이트
                    val createdCount = userDoc.getLong("createdStudiesCount") ?: 0
                    val newCreatedCount = createdCount + 1

                    // 2. 획득한 뱃지 목록 가져오기
                    val earnedBadges = (userDoc.get("earnedBadgeIds") as? List<String> ?: emptyList()).toMutableSet()
                    var newBadgeEarned = false

                    // 3. '스터디 생성' 관련 뱃지 획득 조건 검사
                    if (newCreatedCount == 1L && !earnedBadges.contains("first_study_create")) {
                        earnedBadges.add("first_study_create")
                        newBadgeEarned = true
                    }
                    if (newCreatedCount == 5L && !earnedBadges.contains("five_studies_create")) {
                        earnedBadges.add("five_studies_create")
                        newBadgeEarned = true
                    }

                    // 4. 새로운 스터디 문서 생성
                    transaction.set(newStudyRef, study.copy(studyId = newStudyRef.id))

                    // 5. 사용자 문서 업데이트 내용 구성
                    val userUpdateData = mutableMapOf<String, Any>(
                        "createdStudyIds" to FieldValue.arrayUnion(newStudyRef.id),
                        "createdStudiesCount" to newCreatedCount
                    )
                    if (newBadgeEarned) {
                        userUpdateData["earnedBadgeIds"] = earnedBadges.toList()
                    }
                    transaction.update(userRef, userUpdateData)

                    null // 트랜잭션 성공
                }.await()

                Log.d("StudyDebug", "스터디 저장 및 뱃지 처리 성공: ${newStudyRef.id}")
                clearForm()
                submittedStudies.add(study.copy(studyId = newStudyRef.id))

            } catch (e: Exception) {
                Log.e("StudyDebug", "스터디 저장 실패", e)
                errorMessage = "저장 실패: ${e.message}"
            }
        }
    }

    // ... (clearForm, toggleCategory 함수는 기존과 동일)
    private fun clearForm() {
        title = ""
        startDate = null
        endDate = null
        maxParticipants = ""
        isOffline = false
        minInk = ""
        isRegular = false
        selectedCategories.clear()
        penCount = ""
        punishment = ""
        description = "반갑습니다 ㅎㅎ"
        address = ""
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