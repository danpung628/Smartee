package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartee.model.StudyData
import com.example.smartee.service.SimpleRecommendationService

class RecommendationViewModel(
    app: Application,
    private val authViewModel: AuthViewModel,
    private val userViewModel: UserViewModel
) : AndroidViewModel(app) {
    private val TAG = "RecommendationViewModel"

    private var availableStudies = listOf<StudyData>()

    // AI 추천 서비스
    private val recommendationService = SimpleRecommendationService()

    // 👇 변경: StudyData 대신 ID만 관리
    private val _recommendedStudyId = MutableLiveData<String?>(null)
    val recommendedStudyId: LiveData<String?> = _recommendedStudyId

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _recommendationReason = MutableLiveData<String?>(null)
    val recommendationReason: LiveData<String?> = _recommendationReason

    init {
        userViewModel.userData.observeForever { profile ->
            if (profile != null && profile.interests.isNotEmpty() && availableStudies.isNotEmpty()) {
                Log.d(TAG, "프로필 데이터 로드됨, 카테고리: ${profile.interests}")
                refreshRecommendation(availableStudies)
            }
        }
    }

    // 사용자 관심 카테고리를 UserViewModel에서 가져오기
    private fun getUserCategories(): List<String> {
        return userViewModel.userData.value?.interests ?: listOf()
    }

    private fun getUserInkLevel(): Int {
        return userViewModel.userData.value?.ink ?: 50
    }

    private fun getUserPenLevel(): Int {
        return userViewModel.userData.value?.pen ?: 0
    }

    private fun getUserJoinedStudyIds(): List<String> {
        return userViewModel.userData.value?.joinedStudyIds ?: listOf()
    }

    private fun getUserLocation(): String {
        return userViewModel.userData.value?.region ?: ""
    }

    // 스터디 목록이 변경될 때 추천 새로고침
    fun refreshRecommendation(studies: List<StudyData>) {
        Log.d(TAG, "=== 추천 시작: 받은 스터디 개수 ${studies.size} ===")

        this.availableStudies = studies
        if (studies.isEmpty()) return

        val userCategories = getUserCategories()
        val userInkLevel = getUserInkLevel()
        val userPenLevel = getUserPenLevel()
        val userLocation = getUserLocation()
        val userJoinedStudyIds = getUserJoinedStudyIds()

        Log.d(
            TAG,
            "사용자 정보 - 카테고리: $userCategories, 잉크레벨: $userInkLevel, 만년필: $userPenLevel, 위치: $userLocation"
        )

        if (userCategories.isEmpty()) {
            _errorMessage.value = "관심 카테고리를 설정해주세요"
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        // AI 호출 대신 바로 로컬에서 계산
        val recommendation = recommendationService.recommendStudy(
            userCategories,
            userInkLevel,
            userPenLevel,
            userLocation,
            studies,
            userJoinedStudyIds
        )

        // 👇 변경: StudyData 대신 ID만 저장
        _recommendedStudyId.value = recommendation?.studyId

        // 추천 이유 생성
        if (recommendation != null) {
            val reason = generateRecommendationReason(
                recommendation,
                userCategories,
                userLocation,
                userInkLevel,
                userPenLevel
            )
            _recommendationReason.value = reason
        }

        _isLoading.value = false
        Log.d(TAG, "추천 완료: ${recommendation?.title ?: "추천 없음"}")
    }

    private fun calculateLocationScore(userLocation: String, studyAddress: String): Int {
        if (userLocation.isBlank() || studyAddress.isBlank()) return 0

        // 정확히 포함되면 +3점
        if (studyAddress.contains(userLocation, ignoreCase = true) ||
            userLocation.contains(studyAddress, ignoreCase = true)
        ) {
            return 3
        }

        // 광역시도 전체 단어로 비교 +2점
        val userFirstWord = userLocation.split(" ").firstOrNull()
        val studyFirstWord = studyAddress.split(" ").firstOrNull()

        if (userFirstWord != null && studyFirstWord != null &&
            userFirstWord == studyFirstWord
        ) {
            return 2
        }

        return 0
    }

    private fun generateRecommendationReason(
        study: StudyData,
        userCategories: List<String>,
        userLocation: String,
        userInkLevel: Int,
        userPenLevel: Int
    ): String {
        val reasons = mutableListOf<String>()

        // 1. 카테고리 매칭 (실제 계산 로직과 동일하게)
        val studyCategories = study.category.split(",").map { it.trim() }
        val matchedCategory = userCategories.find { userCategory ->
            studyCategories.any { studyCat ->
                studyCat.equals(userCategory, ignoreCase = true)
            }
        }
        if (matchedCategory != null) {
            reasons.add("'${matchedCategory}' 관심사 일치")
        }

        // 2. 위치 매칭 (실제 계산 로직과 동일하게)
        val locationScore = calculateLocationScore(userLocation, study.address)
        when (locationScore) {
            3 -> reasons.add("'${userLocation}' 지역 정확 매칭")
            2 -> {
                val userFirstWord = userLocation.split(" ").firstOrNull()
                reasons.add("'${userFirstWord}' 광역시도 매칭")
            }
        }

        // 3. 잉크 조건
        if (study.minInkLevel <= userInkLevel) {
            reasons.add("잉크 ${study.minInkLevel} 충족")
        }

        // 4. 만년필 조건
        if (study.penCount <= userPenLevel) {
            reasons.add("만년필 ${study.penCount}개 충족")
        }

        // 5. 정원 여유
        val remainingSlots = study.maxMemberCount - study.participantIds.size
        if (remainingSlots > 0) {
            reasons.add("정원 ${remainingSlots}명 여유")
        }

        return if (reasons.isNotEmpty()) {
            "${reasons.joinToString(" + ")}으로 추천!"
        } else {
            "회원님께 적합한 스터디입니다!"
        }
    }
}

class RecommendationViewModelFactory(
    private val application: Application,
    private val authViewModel: AuthViewModel,
    private val userViewModel: UserViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendationViewModel(application, authViewModel, userViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}