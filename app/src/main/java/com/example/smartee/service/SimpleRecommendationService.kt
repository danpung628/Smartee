// app/src/main/java/com/example/smartee/service/SimpleRecommendationService.kt
package com.example.smartee.service

import com.example.smartee.model.StudyData

class SimpleRecommendationService {

    fun recommendStudy(
        userCategories: List<String>,
        userInkLevel: Int,
        userLocation: String,
        studies: List<StudyData>
    ): StudyData? {

        return studies
            .filter { it.minInkLevel <= userInkLevel } // 잉크 레벨 필터
            .map { study -> study to calculateScore(study, userCategories, userLocation) }
            .filter { it.second > 0 } // 점수 0인 건 제외
            .maxByOrNull { it.second }?.first
    }

    private fun calculateScore(
        study: StudyData,
        userCategories: List<String>,
        userLocation: String
    ): Int {
        var score = 0

        // 1. 카테고리 매칭 (가장 중요) +5점
        if (userCategories.any { study.category.contains(it, ignoreCase = true) }) {
            score += 5
        }

        // 2. 위치 매칭 +3점
        if (study.address.contains(userLocation, ignoreCase = true)) {
            score += 3
        }

        // 3. 인기도 (좋아요 수) 최대 +2점
        score += minOf(study.likeCount / 5, 2)

        // 4. 적당한 참가자 수 (절반~80% 찬 스터디 선호) +1점
        val currentMemberCount = study.participantIds.size  // 여기 수정!
        val memberRatio = currentMemberCount.toFloat() / study.maxMemberCount
        if (memberRatio in 0.5f..0.8f) {
            score += 1
        }

        // 5. 최신 스터디 우대 (createdAt이 있다면 사용, 없으면 생략)
        // 추후 추가 가능

        return score
    }
}