import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
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
fun HostScreen(
    navController: NavController,
    randomCode: Int,
    onCodeGenerated: (Int) -> Unit
) {
    val viewModel: MyStudyViewModel = viewModel()

    val createdStudies by viewModel.myCreatedStudies.collectAsState()
    val selectedStudyMembers by viewModel.selectedStudyMembers.collectAsState()

    var selectedStudy by remember { mutableStateOf<StudyData?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMyStudies()
    }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val isBluetoothOn = bluetoothAdapter?.isEnabled == true

    if (selectedStudy != null && !sessionStarted && isBluetoothOn) {
        Button(onClick = {

        }) {
            Text("출석 세션 시작")
        }
    } else if (!isBluetoothOn) {
        Text("📵 블루투스가 꺼져 있습니다. 켠 후 다시 시도해주세요.")
    }

    val context = LocalContext.current
    LaunchedEffect(sessionStarted) {
        if (sessionStarted) {
            BluetoothServerService(context).start()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("관리자 출석", fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(modifier = Modifier.height(24.dp))

        // ▼ 드롭다운으로 스터디 선택 ▼
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
                        text = { Text(study.title) }, // ✅ 수정된 부분
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

        // ▼ 출석 세션 시작 ▼
        if (selectedStudy != null && !sessionStarted) {
            Button(onClick = {
                sessionStarted = true
                val code = (100..999).random()
                onCodeGenerated(code)
                viewModel.startSession(selectedStudy!!.studyId, code)
            }) {
                Text("출석 세션 시작")
            }
        }

        // ▼ 출석 세션 중 ▼
        if (sessionStarted) {
            Text("출석 세션 시작됨", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("랜덤 코드: $randomCode", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("블루투스 기능 활성화됨", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("출석 현황", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedStudyMembers.isEmpty()) {
                Text(
                    "아직 신청한 사람이 없습니다 😢",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                selectedStudyMembers.forEach { info ->
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
                            Text(if (info.isPresent) "출석 완료 ✅" else "❌ 결석", fontSize = 14.sp)
                            Text("(${info.currentCount}/${info.totalCount})회 출석, 결석 ${info.absentCount}회", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("← 돌아가기")
        }
    }
}

