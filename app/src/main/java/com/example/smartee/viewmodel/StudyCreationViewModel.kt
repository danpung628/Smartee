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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class StudyCreationViewModel : ViewModel() {
    // Firebase 연동
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val studyCollectionRef = db.collection("studies")

    // 스터디 생성 폼 필드
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

    // 누적 저장 리스트
    var submittedStudies = mutableStateListOf<StudyData>()
        private set

    fun validate(): Boolean {
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

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        if (!validate()) {
            errorMessage = "입력값을 확인해주세요."
            return
        }
        errorMessage = null

        // StudyData 객체로 변환
        val newStudy = StudyData(
            ownerId = currentUser.uid,
            participantIds = listOf(currentUser.uid),
            title = title,
            category = selectedCategories.joinToString(","), // 카테고리 문자열로 변환
            dateTimestamp = Timestamp.now(), // 현재 시간
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            isRegular = isRegular,
            //currentMemberCount = 0, // 초기 멤버 수는 0
            maxMemberCount = maxParticipants.toIntOrNull() ?: 0,
            isOffline = isOffline,
            minInkLevel = minInk.toIntOrNull() ?: 0,
            penCount = penCount.toIntOrNull() ?: 0,
            punishment = punishment,
            description = description,
            address = address,
            // 기본값 설정
            commentCount = 0,
            likeCount = 0,
            thumbnailModel = "https://picsum.photos/300/200" // 랜덤 이미지 URL
        )

        // Firebase에 저장
        addStudyToFirebase(newStudy)

        // 로컬 리스트에도 추가
        submittedStudies.add(newStudy)

        // 디버깅용
        Log.d("StudyDebug", "===== 현재까지 생성된 스터디 목록 =====")
        submittedStudies.forEachIndexed { index, study ->
            Log.d("StudyDebug", "${index + 1}. 이름: ${study.title}, 카테고리: ${study.category}")
        }

        // 폼 초기화
        clearForm()
    }

    private fun addStudyToFirebase(study: StudyData) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                //  Firestore의 Batched Write를 사용해 두 개의 작업을 하나로 묶음
                val batch = db.batch()

                // 작업 1: 새로운 스터디 문서 생성
                val newStudyRef = db.collection("studies").document()
                batch.set(newStudyRef, study)

                // 작업 2: 사용자 문서에 새로 만든 스터디 ID 추가
                val userRef = db.collection("users").document(currentUser.uid)
                batch.update(userRef, "createdStudyIds", FieldValue.arrayUnion(newStudyRef.id))

                batch.commit().await()

                Log.d("StudyDebug", "스터디 저장 및 유저 정보 업데이트 성공: ${newStudyRef.id}")
                clearForm()

            } catch (e: Exception) {
                Log.e("StudyDebug", "스터디 저장 실패", e)
                errorMessage = "저장 실패: ${e.message}"
            }
        }
    }

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