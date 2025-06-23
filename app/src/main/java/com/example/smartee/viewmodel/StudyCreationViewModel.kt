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
import com.google.firebase.auth.FirebaseAuth
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

    // [수정] 저장 결과를 처리할 수 있도록 콜백 함수를 파라미터로 추가합니다.
    fun submit(onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("로그인이 필요합니다.")
            return
        }
        if (!validate()) {
            onFailure("입력값을 확인해주세요.")
            return
        }
        errorMessage = null

        val newStudy = StudyData(
            ownerId = currentUser.uid,
            participantIds = listOf(currentUser.uid),
            title = title,
            category = selectedCategories.joinToString(","),
            dateTimestamp = com.google.firebase.Timestamp.now(), // Timestamp 클래스 경로 명시
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

        // [수정] addStudyToFirebase 함수에 콜백을 전달합니다.
        addStudyToFirebase(newStudy, onSuccess, onFailure)
    }

    private fun addStudyToFirebase(study: StudyData, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val userRef = db.collection("users").document(currentUser.uid)
                val newStudyRef = db.collection("studies").document()

                db.runTransaction { transaction ->
                    val userDoc = transaction.get(userRef)
                    val createdCount = userDoc.getLong("createdStudiesCount") ?: 0
                    val newCreatedCount = createdCount + 1
                    val earnedBadges = (userDoc.get("earnedBadgeIds") as? List<String> ?: emptyList()).toMutableSet()
                    var newBadgeEarned = false

                    if (newCreatedCount == 1L && !earnedBadges.contains("first_study_create")) {
                        earnedBadges.add("first_study_create")
                        newBadgeEarned = true
                    }
                    if (newCreatedCount == 5L && !earnedBadges.contains("five_studies_create")) {
                        earnedBadges.add("five_studies_create")
                        newBadgeEarned = true
                    }

                    // 1. 스터디 문서 생성
                    transaction.set(newStudyRef, study.copy(studyId = newStudyRef.id))

                    // ========================= [추가된 코드] =========================
                    // 2. 'members' 하위 컬렉션에 생성자의 초기 출석 데이터 문서를 생성합니다.
                    val initialMemberData = mapOf(
                        "studyName" to study.title,
                        "present" to false,
                        "currentCount" to 0L, // Long 타입 사용
                        "totalCount" to 0L,
                        "absentCount" to 0L
                    )
                    val memberRef = newStudyRef.collection("members").document(currentUser.uid)
                    transaction.set(memberRef, initialMemberData)
                    // ===============================================================

                    // 3. 사용자 정보 업데이트
                    val userUpdateData = mutableMapOf<String, Any>(
                        "createdStudyIds" to com.google.firebase.firestore.FieldValue.arrayUnion(newStudyRef.id),
                        "createdStudiesCount" to newCreatedCount
                    )
                    if (newBadgeEarned) {
                        userUpdateData["earnedBadgeIds"] = earnedBadges.toList()
                    }
                    transaction.update(userRef, userUpdateData)

                    null
                }.await()

                Log.d("StudyDebug", "스터디 저장 및 멤버 초기화, 뱃지 처리 성공: ${newStudyRef.id}")
                clearForm()
                onSuccess(study.title)

            } catch (e: Exception) {
                Log.e("StudyDebug", "스터디 저장 실패", e)
                onFailure("저장 실패: ${e.message}")
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