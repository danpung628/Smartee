// MyStudyScreen.kt

package com.example.smartee.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow // ✅ LazyColumn 대신 LazyRow import
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.model.StudyData
import com.example.smartee.ui.study.studyList.main.StudyListItem
import com.example.smartee.viewmodel.MyStudyViewModel

@Composable
fun MyStudyScreen(
    viewModel: MyStudyViewModel = viewModel(),
    onStudyClick: (String) -> Unit = {}
) {
    val myCreatedStudies by viewModel.myCreatedStudies.collectAsState()
    val myJoinedStudies by viewModel.myJoinedStudies.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyStudies()
    }

    // ✅ 화면 전체를 차지하는 Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // ✅ 상단 영역: 내가 만든 스터디 (화면의 절반 차지)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "내가 만든 스터디",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalStudyList(studies = myCreatedStudies, onClick = onStudyClick)
        }

        // ✅ 하단 영역: 내가 참여 중인 스터디 (화면의 절반 차지)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "내가 참여 중인 스터디",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalStudyList(studies = myJoinedStudies, onClick = onStudyClick)
        }
    }
}

// ✅ 가로 스크롤 리스트를 위한 별도의 Composable
@Composable
fun HorizontalStudyList(
    studies: List<StudyData>,
    onClick: (String) -> Unit
) {
    if (studies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text("해당하는 스터디가 없습니다.")
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(studies) { study ->
                // ✅ 가로 스크롤에 맞게 카드 너비 지정
                Box(modifier = Modifier.width(300.dp)) {
                    StudyListItem(
                        item = study,
                        onClick = { onClick(study.studyId) },
                        isRecommended = false
                    )
                }
            }
        }
    }
}