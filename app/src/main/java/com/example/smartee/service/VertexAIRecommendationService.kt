package com.example.smartee.service

import android.util.Log
import com.example.smartee.model.StudyData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class VertexAIRecommendationService {
    private val TAG = "VertexAIRecommendation"
    private val functions = Firebase.functions

    suspend fun recommendStudy(userCategories: List<String>, userInkLevel: Int): StudyData? {
        try {
            // 현재 사용자 토큰 명시적으로 얻기
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // 토큰 새로 고침 시도
                try {
                    Log.d(TAG, "토큰 새로고침 시도 중...")
                    currentUser.getIdToken(true).await()
                    Log.d(TAG, "토큰 새로고침 성공")
                } catch (e: Exception) {
                    Log.e(TAG, "토큰 새로고침 실패: ${e.message}", e)
                }
            }

            // 요청 데이터 구성
            val data = hashMapOf(
                "userCategories" to userCategories,
                "userInkLevel" to userInkLevel
            )

            // Cloud Function 호출
            val result = functions
                .getHttpsCallable("recommendStudy")
                .call(data)
                .await()

            // 응답 파싱
            val response = result.data as Map<*, *>

            // 추천 이유 로깅
            val reason = response["reason"] as? String
            Log.d(TAG, "추천 이유: $reason")

            // 추천 스터디 파싱
            val recommendedStudy = response["recommendedStudy"] as? Map<*, *>
            if (recommendedStudy != null) {
                // Map을 StudyData 객체로 변환
                return StudyData(
                    studyId = (recommendedStudy["id"] as? String) ?: "",
                    title = (recommendedStudy["title"] as? String) ?: "",
                    category = (recommendedStudy["category"] as? String) ?: "",
                    minInkLevel = (recommendedStudy["minInkLevel"] as? Number)?.toInt() ?: 0,
                    description = (recommendedStudy["description"] as? String) ?: "",
                    currentMemberCount = (recommendedStudy["currentMemberCount"] as? Number)?.toInt() ?: 0,
                    maxMemberCount = (recommendedStudy["maxMemberCount"] as? Number)?.toInt() ?: 0,
                    likeCount = (recommendedStudy["likeCount"] as? Number)?.toInt() ?: 0,
                    commentCount = (recommendedStudy["commentCount"] as? Number)?.toInt() ?: 0,
                    address = (recommendedStudy["address"] as? String) ?: "",
                    thumbnailModel = (recommendedStudy["thumbnailModel"] as? String) ?: ""
                    // 필요한 다른 필드들도 추가
                )
            }

            return null
        } catch (e: Exception) {
            Log.e(TAG, "추천 실패", e)
            return null
        }
    }
}