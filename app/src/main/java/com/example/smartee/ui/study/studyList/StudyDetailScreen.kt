package com.example.smartee.ui.study.studyList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyDetailScreen(
    modifier: Modifier = Modifier,
    studyId: String
) {
    val studyViewModel: StudyViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current,
    )
//    val study = studyViewModel.studyList.find { it.studyId == studyId }

// LiveData를 Compose 상태로 변환
    val studyList by studyViewModel.studyList.observeAsState(mutableListOf())
    // 이제 일반 리스트에서 검색 가능
    val study = studyList.find { it.studyId == studyId }

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
            "분야:" + study.category,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
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