// smartee/ui/attendance/HostScreen.kt

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
import com.example.smartee.model.Meeting
import com.example.smartee.model.StudyData
import com.example.smartee.repository.UserRepository
import com.example.smartee.ui.attendance.AttendanceInfo
import com.example.smartee.viewmodel.MyStudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHostDialog(
    study: StudyData,
    meeting: Meeting,
    randomCode: Int,
    onCodeGenerated: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    // [수정] 테스트 모드 플래그. true로 설정 시 블루투스 체크를 우회합니다.
    // 실제 환경 테스트 시 false로 변경하세요.
    val isTestMode = true

    val viewModel: MyStudyViewModel = viewModel()
    val selectedStudyMembers by viewModel.selectedStudyMembers.collectAsState()
    var sessionStarted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = study.studyId) {
        viewModel.loadMembersForStudy(study.studyId)
    }

    val context = LocalContext.current
    LaunchedEffect(sessionStarted, study) {
        if (sessionStarted) {
            BluetoothServerService(context).start()
        }
    }

    // [수정] isTestMode가 true일 경우, 블루투스가 켜진 것으로 간주합니다.
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val isBluetoothOn = (bluetoothAdapter?.isEnabled == true) || isTestMode

    val currentUserId = UserRepository.getCurrentUserId()
    val isOwner = study.ownerId == currentUserId

    val hasJoinedMeeting = meeting.confirmedParticipants.contains(currentUserId)
    val ownerInfo = selectedStudyMembers.find { it.userId == currentUserId }
    val isOwnerAttended = ownerInfo?.isPresent == true

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
                    text = "'${meeting.title}' 출석 관리",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (!sessionStarted) {
                    if (!isBluetoothOn) {
                        Text("📵 블루투스가 꺼져 있습니다. 켠 후 다시 시도해주세요.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Button(
                            onClick = {
                                sessionStarted = true
                                val code = (100..999).random()
                                onCodeGenerated(code)
                                viewModel.startSession(study.studyId, code)
                            },
                            enabled = isBluetoothOn
                        ) {
                            Text("출석 세션 시작")
                        }
                    }
                }

                if (sessionStarted) {
                    Text("출석 세션 시작됨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("랜덤 코드: $randomCode", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("블루투스 기능 활성화됨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isOwner && hasJoinedMeeting) {
                        Button(
                            onClick = { viewModel.markCurrentUserAsPresent(study.studyId) },
                            enabled = !isOwnerAttended
                        ) {
                            Text(if (isOwnerAttended) "✔ 본인 출석 완료" else "본인 출석하기")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text("출석 현황", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedStudyMembers.isEmpty()) {
                        Text(
                            "아직 출석한 멤버가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        selectedStudyMembers.forEach { info ->
                            AttendeeCard(info)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(onClick = onDismissRequest) {
                    Text("← 돌아가기")
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