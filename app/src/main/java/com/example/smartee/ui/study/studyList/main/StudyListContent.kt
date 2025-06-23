package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isLoading = recommendationViewModel.isLoading.observeAsState(initial = false).value
    val recommendationReason = recommendationViewModel.recommendationReason.observeAsState().value

    Column(modifier = modifier.fillMaxWidth()) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (filteredStudyList.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "검색 결과가 없습니다",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // 추천 스터디가 있으면 최상단에 표시
                recommendedStudy?.let { study ->
                    item {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.surface
                                        )
                                    ),
                                    alpha = 0.7f
                                )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI 추천",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // 추천 이유를 간단한 태그들로 표시
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(extractReasonTags(recommendationReason)) { tag ->
                                            SuggestionChip(
                                                onClick = { },
                                                label = {
                                                    Text(
                                                        text = tag,
                                                        fontSize = 10.sp
                                                    )
                                                },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            StudyListItem(
                                item = study,
                                onClick = onStudyDetailNavigate,
                                isRecommended = true
                            )
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
    }
}

// 추천 이유를 태그로 변환하는 함수
private fun extractReasonTags(reason: String?): List<String> {
    if (reason == null) return emptyList()

    val tags = mutableListOf<String>()

    // 실제 generateRecommendationReason에서 생성하는 패턴에 맞춰서
    if (reason.contains("관심사 일치")) tags.add("관심사 매칭")
    if (reason.contains("지역 정확 매칭")) tags.add("위치 정확")
    else if (reason.contains("광역시도 매칭")) tags.add("광역 근접")
    if (reason.contains("잉크") && reason.contains("충족")) tags.add("잉크 충족")
    if (reason.contains("만년필") && reason.contains("충족")) tags.add("만년필 충족")
    if (reason.contains("정원") && reason.contains("여유")) tags.add("여석 있음")

    return tags.ifEmpty { listOf("추천") }
}