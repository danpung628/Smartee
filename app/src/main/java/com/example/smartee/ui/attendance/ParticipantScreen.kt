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
import com.example.smartee.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantScreen(
    navController: NavController,
    correctCode: Int,
    viewModel: MyStudyViewModel = viewModel()
) {
    val joinedStudies by viewModel.myJoinedStudies.collectAsState()
    var selectedStudy by remember { mutableStateOf<StudyData?>(null) }
    var expanded by remember { mutableStateOf(false) }

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
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStudy?.title ?: "스터디 선택",
                onValueChange = {},
                readOnly = true,
                label = { Text("스터디 선택") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                joinedStudies.forEach { study ->
                    DropdownMenuItem(
                        text = { Text(study.title) },
                        onClick = {
                            selectedStudy = study
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ▼ 블루투스 출석 버튼
        Button(
            enabled = selectedStudy != null,
            onClick = {
                if (selectedStudy == null) return@Button
                result = "블루투스 출석 시도 중..."

                coroutineScope.launch {
                    val service = BluetoothClientService(context)
                    service.sendAttendance(
                        studyId = selectedStudy!!.studyId,
                        userId = UserRepository.getCurrentUserId() ?: "anonymous",
                        code = codeInput.toIntOrNull() ?: 0
                    )
                    result = "출석되었습니다! (블루투스)"
                }
            }
        ) {
            Text("블루투스 출석")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ▼ 코드 입력 출석
        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("코드 입력") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = selectedStudy != null,
            onClick = {
                result = if (codeInput == correctCode.toString()) {
                    "출석되었습니다! (코드 입력)"
                } else {
                    "❌ 코드가 틀렸습니다."
                }
            }) {
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

