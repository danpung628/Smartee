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

    // 추천 스터디 및 상태
    private val _recommendedStudy = MutableLiveData<StudyData?>(null)
    val recommendedStudy: LiveData<StudyData?> = _recommendedStudy

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

    // 스터디 목록이 변경될 때 추천 새로고침
    fun refreshRecommendation(studies: List<StudyData>) {
        Log.d(TAG, "=== 추천 시작: 받은 스터디 개수 ${studies.size} ===")

        this.availableStudies = studies
        if (studies.isEmpty()) return

        val userCategories = getUserCategories()
        val userInkLevel = getUserInkLevel()
        val userLocation = getUserLocation() // 새로 추가

        Log.d(TAG, "사용자 정보 - 카테고리: $userCategories, 잉크레벨: $userInkLevel, 위치: $userLocation")

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
            userLocation,
            studies
        )

        _recommendedStudy.value = recommendation

        // 추천 이유 생성
        if (recommendation != null) {
            val reason = generateRecommendationReason(recommendation, userCategories, userLocation)
            _recommendationReason.value = reason
        }

        _isLoading.value = false
        Log.d(TAG, "추천 완료: ${recommendation?.title ?: "추천 없음"}")
    }

    private fun getUserLocation(): String {
        return userViewModel.userData.value?.region ?: ""  // location → region 변경
    }

    private fun generateRecommendationReason(
        study: StudyData,
        userCategories: List<String>,
        userLocation: String
    ): String {
        val reasons = mutableListOf<String>()

        // 매칭된 관심사 구체적으로 표시
        val matchedCategory = userCategories.find { study.category.contains(it, ignoreCase = true) }
        if (matchedCategory != null) {
            reasons.add("'${matchedCategory}' 관심사 일치")
        }

// 지역 매칭 구체적으로 표시
        if (study.address.contains(userLocation, ignoreCase = true)) {
            reasons.add("'${userLocation}' 지역")
        } else {
            // 광역시도 전체 단어로 비교 (take(2) 제거!)
            val userFirstWord = userLocation.split(" ").firstOrNull()
            val studyFirstWord = study.address.split(" ").firstOrNull()
            if (userFirstWord != null && studyFirstWord != null &&
                userFirstWord == studyFirstWord
            ) {  // take(2) 제거
                reasons.add("'${userFirstWord}' 지역 인근")
            }
        }

        // 잉크 조건 정보
        if (study.minInkLevel > 0) {
            reasons.add("최소 잉크 ${study.minInkLevel} 충족")
        }

        // 인기도 구체적으로 표시
        if (study.likeCount > 10) {
            reasons.add("좋아요 ${study.likeCount}개")
        }

        // 참가자 규모 정보
        val currentMembers = study.participantIds.size
        if (currentMembers >= 3) {
            reasons.add("${currentMembers}명 참여 중")
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