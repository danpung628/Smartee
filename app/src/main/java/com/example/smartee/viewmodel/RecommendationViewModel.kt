package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.service.VertexAIRecommendationService
import kotlinx.coroutines.launch

class RecommendationViewModel(app: Application) : AndroidViewModel(app) {
    private val TAG = "RecommendationViewModel"

    // AI 추천 서비스
    private val recommendationService = VertexAIRecommendationService()

    // 추천 스터디 및 상태
    private val _recommendedStudy = MutableLiveData<StudyData?>(null)
    val recommendedStudy: LiveData<StudyData?> = _recommendedStudy

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // 현재 사용자 관심 카테고리 (실제로는 사용자 프로필에서 가져와야 함)
    // 나중에는 Repository나 UserProfileManager 등에서 가져오도록 변경
    private val userCategories = listOf("프로그래밍", "자격증")
    private val userInkLevel = 70

    // 스터디 목록이 변경될 때 추천 새로고침
    fun refreshRecommendation(availableStudies: List<StudyData>) {
        if (availableStudies.isEmpty()) return

        _isLoading.value = true
        _errorMessage.value = null

        if (userCategories.isEmpty()) {
            _errorMessage.value = "카테고리가 선택되지 않았습니다"
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val recommendation = recommendationService.recommendStudy(
                    userCategories,
                    userInkLevel
                )

                _recommendedStudy.value = recommendation
                Log.d(TAG, "추천 결과: ${recommendation?.title ?: "추천 없음"}")
            } catch (e: Exception) {
                Log.e(TAG, "추천 실패", e)
                _errorMessage.value = "추천을 가져오는 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 사용자 관심사 업데이트 (프로필 변경 시 호출)
    fun updateUserPreferences(categories: List<String>, inkLevel: Int) {
        // 사용자 설정 업데이트 후 추천 갱신
        // 실제 구현에서는 이 정보를 저장하고 관리하는 로직 추가
    }

    // 추천 결과 초기화
    fun clearRecommendation() {
        _recommendedStudy.value = null
    }
}