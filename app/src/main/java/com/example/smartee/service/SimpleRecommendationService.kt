// app/src/main/java/com/example/smartee/service/SimpleRecommendationService.kt
package com.example.smartee.service

import com.example.smartee.model.StudyData
import java.time.LocalDate

class SimpleRecommendationService {

    fun recommendStudy(
        userCategories: List<String>,
        userInkLevel: Int,
        userPenLevel: Int,
        userLocation: String,
        studies: List<StudyData>,
        userJoinedStudyIds: List<String>  // 추가: 사용자가 이미 참여 중인 스터디 ID들
    ): StudyData? {

        // 1차: 모든 조건 만족하는 스터디
        val strictFiltered = studies
            .filter { study ->
                // 기본 조건
                study.minInkLevel <= userInkLevel &&
                        study.penCount <= userPenLevel &&
                        // 필수 조건 (절대 완화 안함)
                        !userJoinedStudyIds.contains(study.studyId) &&  // 중복 참여 방지
                        isStudyNotExpired(study) &&  // 끝나지 않은 스터디만
                        // 완화 가능한 조건
                        study.participantIds.size < study.maxMemberCount  // 정원 여유 있는 것만
            }

        var result = findBestMatch(strictFiltered, userCategories, userLocation)
        if (result != null) return result

        // 2차: 정원 조건 완화 (대기자 등록 가능하다고 가정)
        val relaxedFiltered = studies
            .filter { study ->
                study.minInkLevel <= userInkLevel &&
                        study.penCount <= userPenLevel &&
                        !userJoinedStudyIds.contains(study.studyId) &&
                        isStudyNotExpired(study)
                // 정원 조건 제거
            }

        result = findBestMatch(relaxedFiltered, userCategories, userLocation)
        if (result != null) return result

        // 3차: 잉크/만년필 조건도 완화 (조건 안 맞아도 일단 보여줌)
        val minimalFiltered = studies
            .filter { study ->
                !userJoinedStudyIds.contains(study.studyId) &&
                        isStudyNotExpired(study)
            }

        return findBestMatch(minimalFiltered, userCategories, userLocation)
    }

    private fun isStudyNotExpired(study: StudyData): Boolean {
        return try {
            LocalDate.parse(study.endDate).isAfter(LocalDate.now())
        } catch (e: Exception) {
            true  // 날짜 파싱 실패하면 일단 유효하다고 가정
        }
    }

    private fun findBestMatch(
        studies: List<StudyData>,
        userCategories: List<String>,
        userLocation: String
    ): StudyData? {
        return studies
            .map { study -> study to calculateScore(study, userCategories, userLocation) }
            .filter { it.second > 0 }
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

        // 광역시도 전체 단어로 비교
        val userFirstWord = userLocation.split(" ").firstOrNull()
        val studyFirstWord = studyAddress.split(" ").firstOrNull()

        if (userFirstWord != null && studyFirstWord != null &&
            userFirstWord == studyFirstWord
        ) {  // take(2) 제거
            return 2
        }

        return 0
    }
}