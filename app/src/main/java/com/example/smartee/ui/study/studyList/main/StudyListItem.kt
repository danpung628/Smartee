package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.smartee.model.StudyData
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun StudyListItem(
    modifier: Modifier = Modifier,
    item: StudyData,
    onClick: (String) -> Unit
) {
    Row(
        modifier.clickable {
            onClick(item.studyId)
        }
    ) {
        AsyncImage(
            model = item.thumbnailModel,
            contentDescription = "Study Thumbnail",
            modifier = Modifier.size(100.dp),
        )

        Column {
            Text(item.title)

            Text(
                "${item.address} · ${
                    Duration.between(item.getLocalDateTime(), LocalDateTime.now()).toSeconds()
                }초 전"
            )

            Row {
                Text(
                    if (item.maxMemberCount == Int.MAX_VALUE)
                        "인원 제한 없음"
                    else
                        "${item.currentMemberCount}/${item.maxMemberCount}"
                )
                Spacer(Modifier.width(8.dp))
                Text(item.category)
            }

            Row(//댓글 수, 좋아요 수
                modifier.fillMaxWidth(),
                Arrangement.End
            ) {
                if (item.commentCount > 0) {
                    Row {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Comment,
                            contentDescription = "commentCount",
                        )
                        Text("${item.commentCount}")
                    }
                }
                if (item.likeCount > 0) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "likeCount",
                        )
                        Text("${item.likeCount}")
                    }
                }
            }
        }
    }
}