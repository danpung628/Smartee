package com.example.smartee.model

import study_edit.viewmodel.StudyCreationData
import study_edit.viewmodel.StudyEditViewModel
import java.time.LocalDate

object StudyEditProvider {

    fun provideInitializedViewModel(): StudyEditViewModel {
        val viewModel = StudyEditViewModel()

        val dummyData = StudyCreationData(
            name = "스터디 예시",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(14),
            maxParticipants = 10,
            isOffline = true,
            minInk = 50,
            isRegular = false,
            selectedCategories = listOf("스터디", "자격증"),
            penCount = 2
        )

        viewModel.loadStudyData(dummyData)
        return viewModel
    }
}