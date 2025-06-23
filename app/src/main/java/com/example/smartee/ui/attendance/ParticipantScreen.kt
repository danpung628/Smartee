package com.example.smartee.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartee.model.StudyData
import com.example.smartee.viewmodel.MyStudyViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.bluetooth.BluetoothClientService
import com.example.smartee.model.Meeting
import com.example.smartee.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantScreen(
    navController: NavController,
    viewModel: MyStudyViewModel = viewModel()
) {
    val joinedStudies by viewModel.myJoinedStudies.collectAsState()
    val meetingsForStudy by viewModel.meetingsForStudy.collectAsState()

    var selectedStudy by remember { mutableStateOf<StudyData?>(null) }
    var selectedMeeting by remember { mutableStateOf<Meeting?>(null) }

    var expandedStudy by remember { mutableStateOf(false) }
    var expandedMeeting by remember { mutableStateOf(false) }

    var codeInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadMyStudies()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("참여자 출석", fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(modifier = Modifier.height(24.dp))

        // ▼ 스터디 선택
        ExposedDropdownMenuBox(
            expanded = expandedStudy,
            onExpandedChange = { expandedStudy = !expandedStudy }
        ) {
            OutlinedTextField(
                value = selectedStudy?.title ?: "스터디 선택",
                onValueChange = {},
                readOnly = true,
                label = { Text("스터디 선택") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudy) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedStudy,
                onDismissRequest = { expandedStudy = false }
            ) {
                joinedStudies.forEach { study ->
                    DropdownMenuItem(
                        text = { Text(study.title) },
                        onClick = {
                            selectedStudy = study
                            selectedMeeting = null
                            viewModel.loadMeetingsForStudy(study.studyId)
                            expandedStudy = false
                        }
                    )
                }
            }
        }

        // ▼ 미팅 선택
        if (selectedStudy != null && meetingsForStudy.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = expandedMeeting,
                onExpandedChange = { expandedMeeting = !expandedMeeting }
            ) {
                OutlinedTextField(
                    value = selectedMeeting?.title ?: "모임 선택",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("모임 선택") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMeeting) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedMeeting,
                    onDismissRequest = { expandedMeeting = false }
                ) {
                    meetingsForStudy.forEach { meeting ->
                        DropdownMenuItem(
                            text = { Text(meeting.title) },
                            onClick = {
                                selectedMeeting = meeting
                                expandedMeeting = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ▼ 코드 입력
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("코드 입력") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = selectedStudy != null && selectedMeeting != null,
            onClick = {
                val studyId = selectedStudy!!.studyId
                val meetingId = selectedMeeting!!.meetingId
                viewModel.markAttendanceIfCodeMatches(
                    studyId = studyId,
                    meetingId = meetingId,
                    inputCode = codeInput
                ) { success ->
                    result = if (success) "✅ 출석되었습니다!" else "❌ 코드가 틀렸습니다."
                }
            }
        ) {
            Text("코드로 출석")
        }

        result?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Text(it, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("← 돌아가기")
        }
    }
}

