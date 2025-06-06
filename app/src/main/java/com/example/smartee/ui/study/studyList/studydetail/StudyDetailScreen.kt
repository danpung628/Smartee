package com.example.smartee.ui.study.studyList.studydetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyDetailScreen(
    modifier: Modifier = Modifier,
    studyId: String,
    onJoinStudy: (String) -> Unit = {},
    onReportStudy: (String) -> Unit = {}
) {
    val studyViewModel: StudyViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current,
    )

    val studyList by studyViewModel.studyList.observeAsState(mutableListOf())
    val study = studyList.find { it.studyId == studyId }

    study?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            StudyHeader(study, onReportStudy)
            StudyContent(study)

            // 참가 버튼
            JoinStudyButton(studyId, onJoinStudy)

            Spacer(modifier = Modifier.height(32.dp))
        }
    } ?: run {
        StudyNotFound()
    }
}

@Composable
private fun StudyNotFound() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("스터디 정보를 찾을 수 없습니다.")
    }
}

@Composable
private fun JoinStudyButton(studyId: String, onJoinStudy: (String) -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = { onJoinStudy(studyId) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            "스터디 참가하기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}