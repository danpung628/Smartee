package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.service.VertexAIRecommendationService
import kotlinx.coroutines.launch

class RecommendationViewModel(
    app: Application,
    private val authViewModel: AuthViewModel,
    private val userViewModel: UserViewModel
) : AndroidViewModel(app) {
    private val TAG = "RecommendationViewModel"

    private var availableStudies = listOf<StudyData>()

    // AI 추천 서비스
    private val recommendationService = VertexAIRecommendationService()

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
        studies.forEach { study ->
            Log.d(TAG, "받은 스터디: ${study.title}, 카테고리: ${study.category}")
        }

        val readingStudies = studies.filter { it.category.contains("독서") }
        Log.d(TAG, "독서 스터디 개수: ${readingStudies.size}")

        // 스터디 목록 저장
        this.availableStudies = studies

        if (studies.isEmpty()) return

        val userCategories = getUserCategories()
        val userInkLevel = getUserInkLevel()

        Log.d(TAG, "현재 사용자 카테고리: $userCategories")
        Log.d(TAG, "현재 사용자 잉크레벨: $userInkLevel")

        if (userCategories.isEmpty()) {
            Log.e(TAG, "카테고리가 비어 있어서 추천이 불가능함")
            _errorMessage.value = "관심 카테고리를 설정해주세요"
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val recommendation = recommendationService.recommendStudy(
                    userCategories,
                    userInkLevel
                )

                _recommendedStudy.value = recommendation

                // 추천 이유 설정
                val userName = authViewModel.currentUser.value?.displayName ?: "회원"
                val category = recommendation?.category?.split(",")?.firstOrNull() ?: ""
                val location = if (recommendation?.address?.isNotEmpty() == true)
                    "${recommendation.address} 지역의 "
                else ""

                _recommendationReason.value =
                    "${userName}님이 관심 있는 ${category} 분야의 ${location}스터디입니다"

                Log.d(TAG, "추천 결과: ${recommendation?.title ?: "추천 없음"}")
            } catch (e: Exception) {
                Log.e(TAG, "추천 실패", e)
                _errorMessage.value = "추천을 가져오는 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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