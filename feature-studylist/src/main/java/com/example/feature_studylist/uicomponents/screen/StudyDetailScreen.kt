package com.example.feature_studylist.uicomponents.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.feature_studylist.model.StudyData
import com.example.feature_studylist.viewmodel.StudyViewModel

@Composable
fun StudyDetailScreen(
    modifier: Modifier = Modifier,
    studyId: String
) {
    val study = viewModel<StudyViewModel>().studyList.find { it.studyId == studyId }
    Column {
        AsyncImage(
            model = study!!.thumbnailModel,
            contentDescription = "StudyThumbnail"
        )
        Text(
            study.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp
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