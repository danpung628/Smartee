package com.example.smartee.ui.study.studyList.studydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartee.model.StudyData

@Composable
fun StudyContent(study: StudyData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        StudyCategoryChips(study.category)

        Spacer(modifier = Modifier.height(16.dp))

        StudyInfoCard(study)

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // 스터디 일정 정보 추가
        StudyScheduleInfo(study)

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        StudyDescription(study.description)

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        StudyRequirements(study)

        // 벌칙 정보가 있는 경우에만 표시
        if (study.punishment.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            StudyPunishmentInfo(study)
        }
    }
}