package com.example.feature_studylist.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.example.feature_studylist.model.StudyData
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun StudyListItem(
    modifier: Modifier = Modifier,
    item:StudyData,
) {
    Row {
        AsyncImage(
            model = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp",
            contentDescription = "Study Thumbnail",
        )

        Column {
            Text(item.title)

            Text(
                "${item.address} · ${
                    Duration.between(item.date, LocalDateTime.now()).toSeconds()
                }초 전"
            )

            Text(
                if (item.maxMemberCount == Int.MAX_VALUE)
                    "인원 제한 없음"
                else
                    "${item.currentMemberCount}/${item.maxMemberCount}"
            )

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