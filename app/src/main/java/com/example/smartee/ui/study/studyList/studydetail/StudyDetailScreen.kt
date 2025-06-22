// smartee/ui/study/studyList/studydetail/StudyDetailScreen.kt

package com.example.smartee.ui.study.studyList.studydetail

import AttendanceHostDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartee.model.Meeting
import com.example.smartee.model.ParticipantStatus
import com.example.smartee.navigation.Screen
import com.example.smartee.repository.UserRepository
import com.example.smartee.viewmodel.MeetingStatusViewModel
import com.example.smartee.viewmodel.StudyDetailViewModel
import com.example.smartee.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDetailScreen(
    studyId: String,
    navController: NavController,
    randomCode: Int,
    onCodeGenerated: (Int) -> Unit
) {
    val viewModel: StudyDetailViewModel = viewModel()
    val studyData by viewModel.studyData.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timeUntilNextMeeting by viewModel.timeUntilNextMeeting.collectAsState()
    val pendingRequestCounts by viewModel.pendingRequestCounts.collectAsState()

    val eventState by viewModel.userEvent.collectAsState()
    val showInfoDialog = remember { mutableStateOf<String?>(null) }
    var showManagementDialog by remember { mutableStateOf<Meeting?>(null) }

    var showAttendanceDialog by remember { mutableStateOf(false) }
    var meetingForAttendance by remember { mutableStateOf<Meeting?>(null) }
    val currentUserId = UserRepository.getCurrentUserId()

    var meetingToJoin by remember { mutableStateOf<Meeting?>(null) }
    var meetingToShowInfo by remember { mutableStateOf<Meeting?>(null) }
    var meetingToShowStatus by remember { mutableStateOf<Meeting?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val currentStudyData = studyData
    if (showAttendanceDialog && meetingForAttendance != null && currentStudyData != null) {
        AttendanceHostDialog(
            study = currentStudyData,
            meeting = meetingForAttendance!!,
            randomCode = randomCode,
            onCodeGenerated = onCodeGenerated,
            onDismissRequest = { showAttendanceDialog = false }
        )
    }

    if (meetingToJoin != null) {
        val meeting = meetingToJoin!!
        AlertDialog(
            onDismissRequest = { meetingToJoin = null },
            title = { Text("'${meeting.title}'") },
            text = { Text("해당 모임에 참석을 신청하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.requestToJoinMeeting(meeting)
                    meetingToJoin = null
                }) {
                    Text("신청")
                }
            },
            dismissButton = {
                TextButton(onClick = { meetingToJoin = null }) {
                    Text("취소")
                }
            }
        )
    }

    if (meetingToShowInfo != null) {
        val meeting = meetingToShowInfo!!
        MeetingInfoDialog(
            meeting = meeting,
            pendingRequestCount = pendingRequestCounts[meeting.meetingId] ?: 0,
            onDismiss = { meetingToShowInfo = null },
            onManageClick = {
                meetingToShowInfo = null
                showManagementDialog = meeting
            },
            onRequestListClick = {
                navController.navigate("meeting_request_list/${meeting.meetingId}")
                meetingToShowInfo = null
            }
        )
    }

    if (meetingToShowStatus != null) {
        MeetingStatusDialog(
            meeting = meetingToShowStatus!!,
            onDismiss = { meetingToShowStatus = null }
        )
    }

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
                showInfoDialog.value = "신청이 완료되었습니다."
            }
            is StudyDetailViewModel.UserEvent.JoinConditionsNotMet -> {
                showInfoDialog.value = "스터디 가입 조건(잉크, 만년필)을 충족하지 못했습니다."
            }
            is StudyDetailViewModel.UserEvent.Error -> {
                showInfoDialog.value = event.message
            }
            is StudyDetailViewModel.UserEvent.AlreadyRequested -> {
                showInfoDialog.value = "이미 가입 신청한 스터디입니다."
            }
            null -> {}
        }
    }

    if (showInfoDialog.value != null) {
        AlertDialog(
            onDismissRequest = {
                showInfoDialog.value = null
                viewModel.eventConsumed()
            },
            title = { Text("알림") },
            text = { Text(showInfoDialog.value ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    showInfoDialog.value = null
                    viewModel.eventConsumed()
                }) { Text("확인") }
            }
        )
    }

    if (showManagementDialog != null) {
        val meeting = showManagementDialog!!
        val hasJoined = meeting.confirmedParticipants.contains(currentUserId)
        MeetingManagementDialog(
            onDismiss = { showManagementDialog = null },
            onEdit = {
                navController.navigate("meeting_edit/${meeting.parentStudyId}?meetingId=${meeting.meetingId}")
                showManagementDialog = null
            },
            onAttendanceCheck = {
                meetingForAttendance = meeting
                showAttendanceDialog = true
                showManagementDialog = null
            },
            onJoinMeeting = {
                viewModel.joinMeeting(meeting)
                showManagementDialog = null
            },
            hasJoinedMeeting = hasJoined
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
                    MeetingListSection(
                        meetings = meetings,
                        userRole = userRole,
                        currentUserId = currentUserId,
                        pendingRequestCounts = pendingRequestCounts,
                        onMeetingClick = { clickedMeeting ->
                            val isJoined = clickedMeeting.confirmedParticipants.contains(currentUserId)
                            if (userRole == UserRole.OWNER) {
                                // 관리자는 클릭 시 모임 현황 또는 정보 다이얼로그를 띄움
                                meetingToShowStatus = clickedMeeting
                            } else if (userRole == UserRole.PARTICIPANT) {
                                if (isJoined) {
                                    // 참여자도 가입한 모임은 현황 다이얼로그를 봄
                                    meetingToShowStatus = clickedMeeting
                                } else {
                                    // 가입하지 않은 모임은 가입 신청 다이얼로그를 봄
                                    meetingToJoin = clickedMeeting
                                }
                            }
                        }
                    )
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingInfoDialog(
    meeting: Meeting,
    pendingRequestCount: Int,
    onDismiss: () -> Unit,
    onManageClick: () -> Unit,
    onRequestListClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(meeting.title, style = MaterialTheme.typography.headlineSmall)
                    Row {
                        BadgedBox(
                            badge = {
                                if (pendingRequestCount > 0) {
                                    Badge { Text("$pendingRequestCount") }
                                }
                            }
                        ) {
                            IconButton(onClick = onRequestListClick) {
                                Icon(Icons.Default.People, contentDescription = "신청자 목록")
                            }
                        }
                        IconButton(onClick = onManageClick) {
                            Icon(Icons.Default.Settings, contentDescription = "모임 관리")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("날짜: ${meeting.date}", style = MaterialTheme.typography.bodyLarge)
                Text("시간: ${meeting.time}", style = MaterialTheme.typography.bodyLarge)
                Text("장소: ${meeting.location}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    meeting.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MeetingStatusDialog(
    meeting: Meeting,
    onDismiss: () -> Unit
) {
    val viewModel: MeetingStatusViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MeetingStatusViewModel(meeting) as T
        }
    })

    val participantStatusList by viewModel.participantStatusList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(meeting.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("날짜: ${meeting.date} 시간: ${meeting.time}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(participantStatusList) { participant ->
                            ParticipantStatusCard(participant = participant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantStatusCard(participant: ParticipantStatus) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = participant.thumbnailUrl,
            contentDescription = "${participant.name}의 프로필 사진",
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(participant.name, modifier = Modifier.weight(1f))
        if (participant.isPresent) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "출석 완료", tint = MaterialTheme.colorScheme.primary)
        } else {
            Icon(Icons.Filled.Cancel, contentDescription = "미출석", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun MeetingListSection(
    meetings: List<Meeting>,
    userRole: UserRole,
    currentUserId: String?,
    pendingRequestCounts: Map<String, Int>,
    onMeetingClick: (Meeting) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("예정된 모임", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        if (meetings.isEmpty()) {
            Text("예정된 모임이 없습니다.")
        } else {
            meetings.forEach { meeting ->
                val isJoined = meeting.confirmedParticipants.contains(currentUserId)
                val requestCount = pendingRequestCounts[meeting.meetingId] ?: 0
                MeetingItem(
                    meeting = meeting,
                    isJoined = isJoined,
                    requestCount = if (userRole == UserRole.OWNER) requestCount else 0,
                    onClick = {
                        if (userRole == UserRole.OWNER || userRole == UserRole.PARTICIPANT) {
                            onMeetingClick(meeting)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingItem(
    meeting: Meeting,
    isJoined: Boolean,
    requestCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isJoined) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                Text("날짜: ${meeting.date} 시간: ${meeting.time}")
                Text("장소: ${meeting.location}")
            }
            if (requestCount > 0) {
                BadgedBox(badge = { Badge { Text("$requestCount") } }) {
                    Icon(Icons.Default.Info, contentDescription = "대기중인 신청", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ParticipantButtons(timeUntilNextMeeting: String) {
    val isReadyToAttend = timeUntilNextMeeting == "출석 가능"
    Button(
        onClick = { /* TODO: 참여자의 출석 체크 로직 실행 (블루투스 클라이언트) */ },
        enabled = isReadyToAttend,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(timeUntilNextMeeting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MeetingManagementDialog(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onAttendanceCheck: () -> Unit,
    onJoinMeeting: () -> Unit,
    hasJoinedMeeting: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("모임 관리") },
        text = { Text("수행할 작업을 선택해주세요.") },
        confirmButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!hasJoinedMeeting) {
                    Button(onClick = onJoinMeeting, modifier = Modifier.fillMaxWidth()) {
                        Text("모임 가입")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(onClick = onAttendanceCheck, modifier = Modifier.fillMaxWidth()) {
                    Text("출석 체크")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                    Text("수정")
                }
            }
        },
        dismissButton = {}
    )
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
fun OwnerButtons(navController: NavController, studyId: String) {
    Column {
        Button(
            onClick = {
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