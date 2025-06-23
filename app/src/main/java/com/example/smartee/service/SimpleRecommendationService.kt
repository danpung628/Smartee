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

        // 2. 위치 매칭 (함수 사용!)
        score += calculateLocationScore(userLocation, study.address)

        return score
    }

    private fun calculateLocationScore(userLocation: String, studyAddress: String): Int {
        if (userLocation.isBlank() || studyAddress.isBlank()) return 0

        // 정확히 포함되면 +3점
        if (studyAddress.contains(userLocation, ignoreCase = true) ||
            userLocation.contains(studyAddress, ignoreCase = true)
        ) {
            return 3
        }

        // 첫 번째 단어(광역시도)가 같으면 +2점
        val userFirstWord = userLocation.split(" ").firstOrNull()?.take(2)
        val studyFirstWord = studyAddress.split(" ").firstOrNull()?.take(2)

        if (userFirstWord != null && studyFirstWord != null &&
            userFirstWord == studyFirstWord && userFirstWord.length >= 2
        ) {
            return 2
        }

        return 0
    }
}