package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    onStudyDetailNavigate: (String) -> Unit,
    currentUserId: String
) {
    // 👇 observeAsState() 결과를 직접 받기 (by delegate 제거)
    val filteredStudyList = studyViewModel.filteredStudyList.observeAsState(emptyList()).value
    val recommendedStudyId = recommendationViewModel.recommendedStudyId.observeAsState().value
    val recommendationReason = recommendationViewModel.recommendationReason.observeAsState().value
    val isLoading = recommendationViewModel.isLoading.observeAsState(false).value

    // 추천 스터디는 전체 목록에서 찾기 (항상 최신 데이터)
    val recommendedStudy = filteredStudyList.find { it.studyId == recommendedStudyId }

    Box(modifier = modifier) {
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 추천 스터디 섹션
            if (recommendedStudy != null) {
                item {
                    Column {
                        // 추천 헤더
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "추천",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "오늘의 추천 스터디",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 추천 이유 태그들
                        if (recommendationReason != null) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                val tags = extractReasonTags(recommendationReason)
                                items(tags) { tag -> // 👈 수정: items(tags)로 변경
                                    SuggestionChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = tag,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }

                        StudyListItem(
                            item = recommendedStudy,
                            onClick = onStudyDetailNavigate,
                            onLikeClick = { studyId, userId ->
                                studyViewModel.toggleLike(studyId, userId)
                            },
                            currentUserId = currentUserId,
                            isRecommended = true
                        )
                    }
                }
            }

            // 기존 스터디 목록 (추천 스터디 제외)
            items(filteredStudyList) { study -> // 👈 수정: items() 파라미터 수정
                // 중복 표시 방지
                if (study.studyId != recommendedStudyId) {
                    StudyListItem(
                        item = study,
                        onClick = onStudyDetailNavigate,
                        onLikeClick = { studyId, userId ->
                            studyViewModel.toggleLike(studyId, userId)
                        },
                        currentUserId = currentUserId,
                        isRecommended = false
                    )
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
    else if (reason.contains("광역시도 매칭")) tags.add("위치 근접")
    if (reason.contains("잉크") && reason.contains("충족")) tags.add("잉크 충족")
    if (reason.contains("만년필") && reason.contains("충족")) tags.add("만년필 충족")
    if (reason.contains("정원") && reason.contains("여유")) tags.add("여석 있음")

    return tags.ifEmpty { listOf("추천") }
}