package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartee.viewmodel.RecommendationViewModel
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyListContent(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel,
    recommendationViewModel: RecommendationViewModel,
    onStudyDetailNavigate: (String) -> Unit
) {
    val filteredStudyList = studyViewModel.filteredStudyList.observeAsState(initial = emptyList()).value
    val recommendedStudy = recommendationViewModel.recommendedStudy.observeAsState().value

    LazyColumn {
        // 추천 스터디가 있으면 최상단에 표시
        recommendedStudy?.let { study ->
            item {
                Column {
                    Text(
                        text = "AI 추천 스터디",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    // 추천 스터디 아이템 (특별 스타일 적용)
                    StudyListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        item = study,
                        onClick = onStudyDetailNavigate,
                        isRecommended = true
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        // 기존 스터디 목록
        items(filteredStudyList) { study ->
            // 추천 스터디와 동일한 항목은 중복 표시 방지
            if (recommendedStudy == null || study.studyId != recommendedStudy.studyId) {
                StudyListItem(
                    item = study,
                    onClick = onStudyDetailNavigate,
                    isRecommended = false
                )
            }
        }
    }
}