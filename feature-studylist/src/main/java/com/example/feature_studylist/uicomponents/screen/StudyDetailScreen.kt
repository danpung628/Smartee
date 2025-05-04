package com.example.feature_studylist.uicomponents.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.feature_studylist.viewmodel.StudyViewModel

@Composable
fun StudyDetailScreen(
    modifier: Modifier = Modifier,
    studyId: String
) {
    val study = viewModel<StudyViewModel>().studyList.find { it.studyId == studyId }
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = study!!.thumbnailModel,
            contentDescription = "StudyThumbnail",
            modifier.size(500.dp)
        )
        Text(
            study.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 50.sp
        )
        Text(
            study.description
        )
    }

}

@Preview
@Composable
fun StudyDetailScreenPreview() {
//    StudyDetailScreen()
}