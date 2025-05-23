package com.example.smartee.service

import android.util.Log
import com.example.smartee.model.StudyData
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.tasks.await

class VertexAIRecommendationService {
    private val TAG = "VertexAIRecommendation"
    private val functions = Firebase.functions("us-central1")

    suspend fun recommendStudy(userCategories: List<String>, userInkLevel: Int): StudyData? {
        try {
            if (userCategories.isEmpty()) {
                Log.e(TAG, "카테고리 목록이 비어 있습니다")
                return null
            }

            Log.d(TAG, "요청 데이터: 카테고리=$userCategories, 잉크레벨=$userInkLevel")

            val data = mapOf(
                "categories" to userCategories,
                "inkLevel" to userInkLevel
            )

            Log.d(TAG, "전송할 데이터 타입: ${userCategories.javaClass}")
            Log.d(TAG, "전송할 데이터 내용: $userCategories")
            Log.d(TAG, "최종 요청 데이터: $data")

            Log.d(TAG, "Cloud Function 호출: ${Firebase.app.name}")

            // Cloud Function 호출
            val result = functions.getHttpsCallable("recommendStudy").call(data).await()
            Log.d(TAG, "Cloud Function 응답: ${result.data}")
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
                    currentMemberCount = (recommendedStudy["currentMemberCount"] as? Number)?.toInt()
                        ?: 0,
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