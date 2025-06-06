package com.example.smartee.ui.study.studyList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyDetailScreen(
    navController: NavController,
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
    if (study == null) {
        // 데이터가 없을 때 예외처리 또는 로딩 메시지
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("스터디 정보를 불러올 수 없습니다.")
        }
        return
    }
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
            fontSize = 30.sp
        )
        Text(
            "분야:" + study.category,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp
        )
        Text(
            study.description
        )
        Button(onClick = {
            navController.navigate("studyEdit?studyID=${study.studyId}")

        }) {
            Text("스터디 수정하기")
        }
    }

}

@Preview
@Composable
fun StudyDetailScreenPreview() {
//    StudyDetailScreen()
}