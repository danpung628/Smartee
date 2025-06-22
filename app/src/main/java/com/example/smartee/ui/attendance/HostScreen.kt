// smartee/ui/attendance/HostScreen.kt

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
import com.example.smartee.ui.attendance.AttendanceInfo


@Composable
fun AttendanceHostDialog(
    randomCode: Int,
    onCodeGenerated: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var sessionStarted by remember { mutableStateOf(false) }

    // 임시 출석자 리스트
    val attendeeList = remember {
        listOf(
            AttendanceInfo("알고리즘 스터디", "김길동", true, 3, 5, 2),
            AttendanceInfo("알고리즘 스터디", "이길동", false, 2, 5, 3),
            AttendanceInfo("알고리즘 스터디", "박길동", true, 5, 5, 0),
        )
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
                Text("관리자 출석", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))

                if (!sessionStarted) {
                    Button(onClick = {
                        sessionStarted = true
                        onCodeGenerated((100..999).random())
                    }) {
                        Text("출석 세션 시작")
                    }
                } else {
                    Text("출석 세션 시작됨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("랜덤 코드: $randomCode", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("블루투스 기능 활성화됨", fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text("출석 현황", fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    attendeeList.forEach { info ->
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
            Text("스터디: ${info.studyName}", fontSize = 14.sp)
            Text("이름: ${info.name}", fontSize = 14.sp)
            Text(
                if (info.isPresent) "출석 완료 ✅" else "❌ 결석",
                fontSize = 14.sp
            )
            Text("(${info.currentCount}/${info.totalCount})회 출석, 결석 ${info.absentCount}회", fontSize = 12.sp)
        }
    }
}