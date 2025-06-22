package com.example.smartee.ui.meeting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.viewmodel.MeetingEditViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingEditScreen(
    parentStudyId: String,
    meetingId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MeetingEditViewModel = viewModel()
) {
    LaunchedEffect(key1 = meetingId) {
        if (meetingId != null) {
            viewModel.loadMeeting(meetingId)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isEditMode = meetingId != null
    var showDeleteDialog by remember { mutableStateOf(false) } // [추가] 삭제 확인 다이얼로그 상태

    // [추가] 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("모임 삭제") },
            text = { Text("정말로 이 모임을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMeeting()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }


    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MeetingEditViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is MeetingEditViewModel.UiEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "세부 모임 수정" else "세부 모임 만들기") },
                // [추가] 뒤로가기 버튼
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.title = it }, label = { Text("모임 제목") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            DateSelector(
                label = "날짜 선택",
                selectedDate = viewModel.date,
                onDateSelected = { viewModel.date = it }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                TimeSelector(label = "시작 시간", selectedTime = viewModel.startTime, onTimeSelected = { viewModel.startTime = it }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TimeSelector(label = "종료 시간", selectedTime = viewModel.endTime, onTimeSelected = { viewModel.endTime = it }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("오프라인 모임")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = viewModel.isOffline, onCheckedChange = { viewModel.isOffline = it })
            }
            if (viewModel.isOffline) {
                OutlinedTextField(value = viewModel.location, onValueChange = { viewModel.location = it }, label = { Text("모임 장소") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = viewModel.maxParticipants, onValueChange = { viewModel.maxParticipants = it }, label = { Text("최대 참여 인원 (0은 무제한)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = viewModel.description, onValueChange = { viewModel.description = it }, label = { Text("간단한 설명") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { viewModel.saveMeeting(parentStudyId) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEditMode) "수정 완료" else "생성 완료")
            }

            // [추가] 수정 모드일 때만 삭제 버튼 표시
            if (isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("모임 삭제")
                }
            }
        }
    }
}
@Composable
fun DateSelector(label: String, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
        Text(selectedDate?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) ?: label)
    }
}

@Composable
fun TimeSelector(label: String, selectedTime: LocalTime?, onTimeSelected: (LocalTime) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    OutlinedButton(onClick = { timePickerDialog.show() }, modifier = modifier) {
        Text(selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: label)
    }
}