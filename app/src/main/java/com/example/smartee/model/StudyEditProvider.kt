package com.example.smartee.model

import com.example.smartee.viewmodel.StudyEditViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate

object StudyEditProvider {

    fun provideInitializedViewModel(): StudyEditViewModel {
        val viewModel = StudyEditViewModel()

        val dummyData = StudyData(
            studyId = "", // 빈 ID (또는 임시 ID "dummy_id")
            title = "스터디 예시",
            category = "스터디,자격증", // 콤마로 구분된 카테고리 문자열
            dateTimestamp = Timestamp.now(), // 현재 시간
            startDate = LocalDate.now().toString(),
            endDate = LocalDate.now().plusDays(14).toString(),
            isRegular = false,
            currentMemberCount = 0,
            maxMemberCount = 10,
            isOffline = true,
            minInkLevel = 50,
            penCount = 2,
            punishment = "",
            description = "반갑습니다 ㅎㅎ", // 기본 설명
            address = "", // 빈 주소 또는 기본값 설정
            commentCount = 0,
            likeCount = 0,
            thumbnailModel = "https://picsum.photos/300/200" // 랜덤 이미지 URL
        )

        viewModel.loadStudyData(dummyData)
        return viewModel
    }
}