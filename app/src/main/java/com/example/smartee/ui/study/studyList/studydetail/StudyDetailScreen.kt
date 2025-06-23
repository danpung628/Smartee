// smartee/ui/study/studyList/studydetail/StudyDetailScreen.kt

package com.example.smartee.ui.study.studyList.studydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartee.bluetooth.BluetoothClientService
import com.example.smartee.bluetooth.BluetoothServerService
import com.example.smartee.model.Meeting
import com.example.smartee.model.ParticipantStatus
import com.example.smartee.model.StudyData
import com.example.smartee.repository.UserRepository
import com.example.smartee.viewmodel.StudyDetailViewModel
import com.example.smartee.viewmodel.UserRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StudyDetailScreen(
    studyId: String,
    navController: NavController
) {
    val viewModel: StudyDetailViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val studyData by viewModel.studyData.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeMeetingSessions by viewModel.activeMeetingSessions.collectAsState()
    val participantStatusList by viewModel.participantStatusList.collectAsState()
    val eventState by viewModel.userEvent.collectAsState()
    val currentUserId = UserRepository.getCurrentUserId()
    // [추가] ViewModel에서 생성된 출석 코드를 구독합니다.
    val generatedCode by viewModel.generatedAttendanceCode.collectAsState()
    val pendingRequestCount by viewModel.pendingRequestCount.collectAsState()

    val isRefreshing = isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadStudy(studyId) }
    )

    var meetingForDialog by remember { mutableStateOf<Meeting?>(null) }
    var showHostSettingsMenu by remember { mutableStateOf(false) }
    // [수정] 다이얼로그 상태 변수가 이제 Meeting 객체 자체를 저장합니다.
    var meetingForHostAttendance by remember { mutableStateOf<Meeting?>(null) }
    val showInfoDialog = remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStudy(studyId)
                viewModel.loadPendingRequestCount()

            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(meetingForDialog) {
        val meeting = meetingForDialog
        if (meeting != null) {
            viewModel.listenForParticipantStatus(meeting)
        } else {
            viewModel.stopListeningForParticipantStatus()
        }
    }

    LaunchedEffect(eventState) {
        when (val event = eventState) {
            is StudyDetailViewModel.UserEvent.RequestSentSuccessfully -> {
                showInfoDialog.value = "가입 요청이 성공적으로 전송되었습니다."
                meetingForDialog = null
            }
            is StudyDetailViewModel.UserEvent.WithdrawSuccessful -> {
                meetingForDialog = null
                viewModel.eventConsumed()
            }
            is StudyDetailViewModel.UserEvent.Error -> showInfoDialog.value = event.message
            else -> {}
        }
    }

    if (meetingForDialog != null) {
        UnifiedMeetingDialog(
            meeting = meetingForDialog!!,
            userRole = userRole,
            isSessionActive = activeMeetingSessions[meetingForDialog!!.meetingId] ?: false,
            participantStatusList = participantStatusList,
            currentUserId = currentUserId ?: "",
            onDismiss = { meetingForDialog = null },
            onStartAttendance = {
                // [수정] 출석 세션 시작 시, ViewModel의 함수를 호출하고 다이얼로그를 엽니다.
                viewModel.startAttendanceSession(it.meetingId)
                meetingForHostAttendance = it
                meetingForDialog = null
            },
            onWithdraw = { viewModel.withdrawFromMeeting(it.meetingId) },
            onAttend = { meeting ->
                viewModel.performBluetoothAttendance(meeting)
            },
            onManageRequests = {
                navController.navigate("meeting_request_list/${it.meetingId}")
                meetingForDialog = null
            },
            onEditMeeting = {
                navController.navigate("meeting_edit/${it.parentStudyId}?meetingId=${it.meetingId}")
                meetingForDialog = null
            },
            onDeleteMeeting = {
                viewModel.deleteMeeting(it)
                meetingForDialog = null
            },
            onRequestToJoin = { viewModel.requestToJoinMeeting(it) }
        )
    }

    // [수정] 플레이스홀더를 실제 다이얼로그로 교체하여 호출
    if (meetingForHostAttendance != null) {
        // 출석 현황은 UnifiedMeetingDialog와 동일한 participantStatusList를 사용합니다.
        LaunchedEffect(meetingForHostAttendance) {
            viewModel.listenForParticipantStatus(meetingForHostAttendance!!)
        }

        AttendanceHostDialog(
            meeting = meetingForHostAttendance!!,
            generatedCode = generatedCode,
            attendees = participantStatusList,
            onDismissRequest = { meetingForHostAttendance = null },
            onMarkSelfAsPresent = { viewModel.markHostAsPresent(meetingForHostAttendance!!.meetingId) }
        )
    }

    if (showInfoDialog.value != null) {
        AlertDialog(
            onDismissRequest = { showInfoDialog.value = null; viewModel.eventConsumed() },
            title = { Text("알림") },
            text = { Text(showInfoDialog.value ?: "") },
            confirmButton = { TextButton(onClick = { showInfoDialog.value = null; viewModel.eventConsumed() }) { Text("확인") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        val study = studyData
        if (isLoading && study == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (study != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    StudyHeader(study = study, onReportStudy = viewModel::reportStudy)
                    StudyContent(study = study)

                    if (userRole == UserRole.OWNER || userRole == UserRole.PARTICIPANT) {
                        MeetingListSection(
                            meetings = meetings,
                            activeMeetingSessions = activeMeetingSessions,
                            onMeetingClick = { clickedMeeting ->
                                meetingForDialog = clickedMeeting
                            },
                            currentUserId = currentUserId
                        )
                    }
                }
                Box(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                    when (userRole) {
                        UserRole.OWNER -> OwnerButtons(navController, study.studyId,pendingRequestCount)
                        UserRole.GUEST -> GuestButtons(viewModel, isLoading)
                        UserRole.PARTICIPANT -> {}
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                StudyNotFound()
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}




@Composable
private fun MeetingListSection(
    meetings: List<Meeting>,
    activeMeetingSessions: Map<String, Boolean>,
    onMeetingClick: (Meeting) -> Unit,
    currentUserId: String?
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("예정된 모임", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        if (meetings.isEmpty()) {
            Text("예정된 모임이 없습니다.")
        } else {
            meetings.forEach { meeting ->
                val isSessionActive = activeMeetingSessions[meeting.meetingId] ?: false
                val isJoined = meeting.confirmedParticipants.contains(currentUserId)
                MeetingItem(
                    meeting = meeting,
                    isSessionActive = isSessionActive,
                    isJoined = isJoined,
                    onCardClick = { onMeetingClick(meeting) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingItem(
    meeting: Meeting,
    isSessionActive: Boolean,
    isJoined: Boolean,
    onCardClick: () -> Unit
) {
    val cardColor = when {
        isSessionActive -> MaterialTheme.colorScheme.primaryContainer
        isJoined -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (isSessionActive) {
                    Text("✅ 출석 진행 중", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                Text("날짜: ${meeting.date} 시간: ${meeting.time}", style = MaterialTheme.typography.bodyMedium)
                Text("장소: ${meeting.location}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@Composable
private fun UnifiedMeetingDialog(
    meeting: Meeting,
    userRole: UserRole,
    isSessionActive: Boolean,
    participantStatusList: List<ParticipantStatus>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onStartAttendance: (Meeting) -> Unit,
    onWithdraw: (Meeting) -> Unit,
    onAttend: (Meeting) -> Unit,
    onManageRequests: (Meeting) -> Unit,
    onEditMeeting: (Meeting) -> Unit,
    onDeleteMeeting: (Meeting) -> Unit,
    onRequestToJoin: (Meeting) -> Unit
) {
    val isJoined = meeting.confirmedParticipants.contains(currentUserId)
    val amIPresent = participantStatusList.find { it.userId == currentUserId }?.isPresent == true
    var showHostSettingsMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(meeting.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    if (userRole == UserRole.OWNER) {
                        IconButton(onClick = { onManageRequests(meeting) }) { Icon(Icons.Default.People, "신청자 목록") }

                        // [수정] 설정 버튼과 드롭다운 메뉴를 Box로 함께 묶습니다.
                        Box {
                            IconButton(onClick = { showHostSettingsMenu = true }) {
                                Icon(Icons.Default.Settings, "설정")
                            }
                            DropdownMenu(
                                expanded = showHostSettingsMenu,
                                onDismissRequest = { showHostSettingsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("모임 수정") },
                                    onClick = {
                                        onEditMeeting(meeting)
                                        showHostSettingsMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("모임 삭제") },
                                    onClick = {
                                        onDeleteMeeting(meeting)
                                        showHostSettingsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                Text("날짜: ${meeting.date} 시간: ${meeting.time}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text("참여자 현황", style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                if (participantStatusList.isEmpty()) {
                    Text("참여자가 없습니다.", modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(participantStatusList) { participant ->
                            ParticipantStatusRow(participant = participant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (userRole == UserRole.PARTICIPANT) {
                    if (isJoined) {
                        if (isSessionActive && !amIPresent) {
                            Button(onClick = { onAttend(meeting) }, modifier = Modifier.fillMaxWidth()) {
                                Text("블루투스로 출석하기")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { onWithdraw(meeting) }, modifier = Modifier.fillMaxWidth()) {
                            Text("참여 취소")
                        }
                    } else {
                        Button(onClick = { onRequestToJoin(meeting) }, modifier = Modifier.fillMaxWidth()) {
                            Text("참여 요청하기")
                        }
                    }
                }
                if (userRole == UserRole.OWNER) {
                    Button(onClick = { onStartAttendance(meeting) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isSessionActive) "출석 세션 진행 중" else "출석 세션 시작")
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantStatusRow(participant: ParticipantStatus) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = participant.thumbnailUrl.ifEmpty { "https://picsum.photos/id/1/200" },
            contentDescription = "${participant.name}의 프로필 사진",
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(participant.name, modifier = Modifier.weight(1f))
        if (participant.isPresent) {
            Icon(Icons.Filled.CheckCircle, "출석 완료", tint = MaterialTheme.colorScheme.primary)
        } else {
            Icon(Icons.Filled.Cancel, "미출석", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun AttendanceHostPlaceholderDialog(meetingTitle: String, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("'$meetingTitle' 출석 세션") },
        text = { Text("이곳에 기존의 관리자 출석 화면(AttendanceHostDialog)의 실제 구현이 필요합니다.") },
        confirmButton = { TextButton(onClick = onDismissRequest) { Text("닫기") } }
    )
}

@Composable
private fun StudyNotFound() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("스터디 정보를 찾을 수 없습니다.")
    }
}

@Composable
private fun OwnerButtons(
    navController: NavController,
    studyId: String,
    pendingRequestCount: Int
) {
    Column {
        Button(
            onClick = { navController.navigate("meeting_edit/$studyId") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("세부 모임 추가") }
        Spacer(modifier = Modifier.height(8.dp))

        // [수정] Row에 Arrangement를 추가하여 버튼 사이 간격을 줍니다.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // [수정] Box가 Row의 공간을 1만큼 차지하도록 weight(1f)를 적용합니다.
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { navController.navigate("request_list") },
                    // Button은 Box 내부를 꽉 채웁니다.
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("가입 요청 관리")
                }

                if (pendingRequestCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 4.dp, top = 4.dp)
                    ) {
                        Text("$pendingRequestCount")
                    }
                }
            }

            // [수정] OutlinedButton도 동일하게 weight(1f)를 적용하여 1:1 비율을 맞춥니다.
            OutlinedButton(
                onClick = { navController.navigate("study_edit?studyID=$studyId") },
                modifier = Modifier.weight(1f)
            ) {
                Text("스터디 편집")
            }
        }
    }
}

@Composable
private fun GuestButtons(viewModel: StudyDetailViewModel, isLoading: Boolean) {
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
private fun AttendanceHostDialog(
    meeting: Meeting,
    generatedCode: Int?,
    attendees: List<ParticipantStatus>,
    onDismissRequest: () -> Unit,
    onMarkSelfAsPresent: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = UserRepository.getCurrentUserId()
    val isSelfAttended = attendees.find { it.userId == currentUserId }?.isPresent == true

    // 세션이 시작되면 블루투스 서버를 활성화
    LaunchedEffect(Unit) {
        BluetoothServerService(context).start()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "'${meeting.title}' 출석 세션",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text("출석 세션 시작됨", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // 랜덤 코드 표시
                if (generatedCode != null) {
                    Text("랜덤 코드: $generatedCode", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("블루투스 기능 활성화됨", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(24.dp))

                // 본인 출석하기 버튼
                Button(
                    onClick = onMarkSelfAsPresent,
                    enabled = !isSelfAttended
                ) {
                    Text(if (isSelfAttended) "✔ 본인 출석 완료" else "본인 출석하기")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("출석 현황", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                // 실시간 출석 현황 목록
                if (attendees.isEmpty()) {
                    Text("아직 출석한 멤버가 없습니다.", modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(attendees) { participant ->
                            ParticipantStatusRow(participant = participant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(onClick = onDismissRequest, modifier = Modifier.fillMaxWidth()) {
                    Text("닫기")
                }
            }
        }
    }
}