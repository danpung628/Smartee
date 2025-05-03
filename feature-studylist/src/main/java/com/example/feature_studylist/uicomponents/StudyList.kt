package com.example.feature_studylist.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.example.feature_studylist.model.StudyData
import java.time.LocalDateTime
import java.time.Duration

@Composable
fun StudyList(
    modifier: Modifier = Modifier,
    studyList: MutableList<StudyData>
) {
    Column {
        Row {
            AddressList()

        }
        studyList.forEach {
            Row {
                AsyncImage(
                    model = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp",
                    contentDescription = "Study Thumbnail",
                )
                Column {
                    Text(it.title)
                    Text(
                        "${it.address} · ${
                            Duration.between(it.date, LocalDateTime.now()).toSeconds()
                        } 전"
                    )
                    Text(
                        if (it.maxMemberCount == Int.MAX_VALUE)
                            "인원 제한 없음"
                        else
                            "${it.currentMemberCount}/${it.maxMemberCount}"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun StudyListPreview() {

}