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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.viewmodel.StudyDetailViewModel

@Composable
fun StudyDetailScreen(
    studyId: String,
) {
    val viewModel: StudyDetailViewModel = viewModel()
    val studyData by viewModel.studyData.collectAsState()
    val userEvent by viewModel.userEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val showDialog = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = studyId) {
        viewModel.loadStudy(studyId)
    }

    LaunchedEffect(key1 = userEvent) {
        when (val event = userEvent) {
            is StudyDetailViewModel.UserEvent.RequestSentSuccessfully -> {
                showDialog.value = "스터디 가입 신청이 완료되었습니다."
            }
            is StudyDetailViewModel.UserEvent.JoinConditionsNotMet -> {
                showDialog.value = "스터디 가입 조건(잉크, 만년필)을 충족하지 못했습니다."
            }
            is StudyDetailViewModel.UserEvent.Error -> {
                showDialog.value = event.message
            }
            is StudyDetailViewModel.UserEvent.AlreadyRequested -> {
                showDialog.value = "이미 가입 신청한 스터디입니다."
            }
            null -> {}
        }
    }

    if (showDialog.value != null) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = null
                viewModel.eventConsumed()
            },
            title = { Text("알림") },
            text = { Text(showDialog.value ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog.value = null
                    viewModel.eventConsumed()
                }) {
                    Text("확인")
                }
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        studyData?.let { study ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                StudyHeader(study, onReportStudy = viewModel::reportStudy)
                StudyContent(study)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.requestToJoinStudy() },
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            StudyNotFound()
        }
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