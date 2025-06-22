package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.smartee.model.StudyData
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun StudyListItem(
    modifier: Modifier = Modifier,
    item: StudyData,
    onClick: (String) -> Unit,
    isRecommended: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(item.studyId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 썸네일 이미지 (비율 최적화)
            AsyncImage(
                model = item.thumbnailModel,
                contentDescription = "Study Thumbnail",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 제목 행 (추천 아이콘 포함)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (isRecommended) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI 추천",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 장소 및 시간 정보
                Text(
                    text = "${item.address} · ${getTimeAgo(item.getLocalDateTime())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 카테고리를 칩으로 표시
                FlowRow {
                    item.category.split(",").forEach { category ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(category) },
                            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 하단 정보 (인원, 댓글, 좋아요)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.maxMemberCount == Int.MAX_VALUE)
                            "인원 제한 없음"
                        else
                            "${item.participantIds.size}/${item.maxMemberCount}명", // 참여자 리스트의 크기를 직접 사용
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Default.Comment,
                                contentDescription = "commentCount",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${item.commentCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "likeCount",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${item.likeCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// 시간 포맷팅 헬퍼 함수
private fun getTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(dateTime, now)

    return when {
        duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
        duration.toHours() < 24 -> "${duration.toHours()}시간 전"
        duration.toDays() < 30 -> "${duration.toDays()}일 전"
        else -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}