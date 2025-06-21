package com.example.smartee.ui.study.studyList.studydetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.navigation.Screen
import com.example.smartee.viewmodel.StudyDetailViewModel

@Composable
fun StudyDetailScreen(
    studyId: String,
    navController: NavController
) {
    val viewModel: StudyDetailViewModel = viewModel()
    val studyData by viewModel.studyData.collectAsState()
    val userEvent by viewModel.userEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
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

    val study = studyData
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (study != null) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            StudyHeader(study, onReportStudy = viewModel::reportStudy)
            StudyContent(study)
            Spacer(modifier = Modifier.weight(1f))

            if (isOwner) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(onClick = { navController.navigate("request_list/${study.studyId}") }, modifier = Modifier.weight(1f)) {
                        Text("가입 요청 관리")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { navController.navigate(Screen.StudyEdit.route + "?studyID=${study.studyId}") }, modifier = Modifier.weight(1f)) {
                        Text("스터디 편집")
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.requestToJoinStudy() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("스터디 참가하기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    } else {
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