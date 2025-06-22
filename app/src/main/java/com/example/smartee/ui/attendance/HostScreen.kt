import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.bluetooth.BluetoothServerService
import com.example.smartee.model.StudyData
import com.example.smartee.ui.attendance.AttendanceInfo
import com.example.smartee.viewmodel.MyStudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHostDialog(
    randomCode: Int,
    onCodeGenerated: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val viewModel: MyStudyViewModel = viewModel()

    // ViewModel로부터 생성한 스터디 목록과 선택된 스터디의 멤버 목록을 가져옵니다.
    val createdStudies by viewModel.myCreatedStudies.collectAsState()
    val selectedStudyMembers by viewModel.selectedStudyMembers.collectAsState()

    // UI 상태 관리를 위한 변수들을 선언합니다.
    var selectedStudy by remember { mutableStateOf<StudyData?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }

    // Composable이 처음 로드될 때 내가 생성한 스터디 목록을 불러옵니다.
    LaunchedEffect(Unit) {
        viewModel.loadMyStudies()
    }

    val context = LocalContext.current
    // 출석 세션이 시작되면 블루투스 서버 서비스를 실행합니다.
    LaunchedEffect(sessionStarted, selectedStudy) {
        if (sessionStarted && selectedStudy != null) {
            BluetoothServerService(context).start()
        }
    }

    // 블루투스 활성화 상태를 확인합니다.
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val isBluetoothOn = bluetoothAdapter?.isEnabled == true

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
                Text("관리자 출석", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))

                // 출석 세션이 시작되지 않았을 때만 스터디 선택 드롭다운을 표시합니다.
                if (!sessionStarted) {
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
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            createdStudies.forEach { study ->
                                DropdownMenuItem(
                                    text = { Text(study.title) },
                                    onClick = {
                                        selectedStudy = study
                                        viewModel.loadMembersForStudy(study.studyId)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }


                // 스터디가 선택되었고 세션이 시작되지 않았을 때
                if (selectedStudy != null && !sessionStarted) {
                    if (!isBluetoothOn) {
                        Text("📵 블루투스가 꺼져 있습니다. 켠 후 다시 시도해주세요.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Button(
                            onClick = {
                                sessionStarted = true
                                val code = (100..999).random()
                                onCodeGenerated(code)
                                selectedStudy?.let {
                                    viewModel.startSession(it.studyId, code)
                                }
                            },
                            enabled = isBluetoothOn // 블루투스가 켜져있을 때만 버튼 활성화
                        ) {
                            Text("출석 세션 시작")
                        }
                    }
                }

                // 출석 세션이 시작되었을 때
                if (sessionStarted) {
                    Text("출석 세션 시작됨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("랜덤 코드: $randomCode", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("블루투스 기능 활성화됨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(32.dp))

                    Text("출석 현황", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedStudyMembers.isEmpty()) {
                        Text(
                            "아직 출석한 멤버가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        selectedStudyMembers.forEach { info ->
                            AttendeeCard(info)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onDismissRequest) {
                        Text("← 돌아가기")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(info: AttendanceInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (info.isPresent) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("이름: ${info.name}", fontSize = 14.sp)
            Text(
                if (info.isPresent) "출석 완료 ✅" else "❌ 결석",
                fontSize = 14.sp
            )
            Text("(${info.currentCount}/${info.totalCount})회 출석, 결석 ${info.absentCount}회", fontSize = 12.sp)
        }
    }
}