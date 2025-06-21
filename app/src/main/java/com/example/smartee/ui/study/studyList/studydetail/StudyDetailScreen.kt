package com.example.smartee.ui.study.studyList.studydetail

import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.model.Meeting
import com.example.smartee.navigation.Screen
import com.example.smartee.viewmodel.StudyDetailViewModel
import com.example.smartee.viewmodel.UserRole

@Composable
fun StudyDetailScreen(
    studyId: String,
    navController: NavController
) {

    val viewModel: StudyDetailViewModel = viewModel()
    val studyData by viewModel.studyData.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timeUntilNextMeeting by viewModel.timeUntilNextMeeting.collectAsState()


    Log.d("MEETING_DEBUG", "화면 재구성됨. meetings 리스트 크기: ${meetings.size}")

    val eventState by viewModel.userEvent.collectAsState()
    val showDialog = remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // 화면이 다시 활성화될 때마다(예: 이전 화면에서 돌아올 때) 데이터를 새로고침
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStudy(studyId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = eventState) {
        when (val event = eventState) {
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
    if (isLoading && study == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (study != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                StudyHeader(study, onReportStudy = viewModel::reportStudy)
                StudyContent(study)

                if (userRole == UserRole.OWNER || userRole == UserRole.PARTICIPANT) {
                    MeetingListSection(meetings = meetings)
                }
            }

            Box(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                when (userRole) {
                    UserRole.OWNER -> OwnerButtons(navController, study.studyId)
                    UserRole.PARTICIPANT -> ParticipantButtons(timeUntilNextMeeting)
                    UserRole.GUEST -> GuestButtons(viewModel, isLoading)
                }
            }
        }
    } else {
        StudyNotFound()
    }
}

@Composable
fun OwnerButtons(navController: NavController, studyId: String) {
    Column {
        Button(
            onClick = {
                // 네비게이션 직전에 studyId 값을 로그로 출력
                Log.d("ID_TRACE", "StudyDetailScreen에서 전달하는 ID: $studyId")
                navController.navigate("create_meeting/$studyId")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("세부 모임 추가")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { navController.navigate("request_list/$studyId") }, modifier = Modifier.weight(1f)) {
                Text("가입 요청 관리")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = { navController.navigate(Screen.StudyEdit.route + "?studyID=$studyId") }, modifier = Modifier.weight(1f)) {
                Text("스터디 편집")
            }
        }
    }
}
@Composable
fun ParticipantButtons(timeUntilNextMeeting: String) {
    Button(
        onClick = { /* TODO: 출석 체크 로직 실행 */ },
        enabled = timeUntilNextMeeting == "출석 가능",
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        val buttonText = if (timeUntilNextMeeting.isNotEmpty()) "출석하기 ($timeUntilNextMeeting)" else "예정된 모임 없음"
        Text(buttonText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GuestButtons(viewModel: StudyDetailViewModel, isLoading: Boolean) {
    Button(
        onClick = { viewModel.requestToJoinStudy() },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text("스터디 참가하기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MeetingListSection(meetings: List<Meeting>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("예정된 모임", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        if (meetings.isEmpty()) {
            Text("예정된 모임이 없습니다.")
        } else {
            meetings.forEach { meeting ->
                MeetingItem(meeting = meeting)
            }
        }
    }
}

@Composable
fun MeetingItem(meeting: Meeting) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(meeting.title, style = MaterialTheme.typography.titleMedium)
            Text("날짜: ${meeting.date} 시간: ${meeting.time}")
            Text("장소: ${meeting.location}")
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

