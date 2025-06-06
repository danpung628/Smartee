package com.example.smartee.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.model.StudyData
import com.example.smartee.ui.study.studyList.main.StudyListItem  // 홈 화면에서 사용된 StudyListItem 가져오기
import com.example.smartee.viewmodel.MyStudyViewModel

@Composable
fun MyStudyScreen(
    viewModel: MyStudyViewModel = viewModel(),
    onStudyClick: (String) -> Unit = {}  // 상세 화면 이동 등 클릭 처리용 람다
) {
    val myCreatedStudies by viewModel.myCreatedStudies.collectAsState()
    val myJoinedStudies by viewModel.myJoinedStudies.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyStudies()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "내가 만든 스터디",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        StudyList(studies = myCreatedStudies, onClick = onStudyClick)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "내가 참여 중인 스터디",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        StudyList(studies = myJoinedStudies, onClick = onStudyClick)
    }
}

@Composable
fun StudyList(
    studies: List<StudyData>,
    onClick: (String) -> Unit
) {
    LazyColumn {
        items(studies) { study ->
            StudyListItem(
                item = study,
                onClick = { onClick(it) },
                isRecommended = false
            )
        }
    }
}
