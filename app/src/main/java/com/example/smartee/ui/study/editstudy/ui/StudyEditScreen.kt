package com.example.smateeeeeeeeeeeeeeeeeeeeeeeee.editstudy.ui
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smateeeeeeeeeeeeeeeeeeeeeeeee.editstudy.util.validateStudy
import kotlinx.coroutines.launch
import study_edit.ui.component.CategorySelector
import study_edit.ui.component.DatePickerField
import study_edit.viewmodel.StudyCreationData
import study_edit.viewmodel.StudyEditViewModel

@Composable
fun StudyEditScreen(viewModel: StudyEditViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("스터디 이름") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            DatePickerField("시작일", viewModel.startDate) { viewModel.startDate = it }
            DatePickerField("종료일", viewModel.endDate) { viewModel.endDate = it }

            OutlinedTextField(
                value = viewModel.maxParticipants,
                onValueChange = { viewModel.maxParticipants = it },
                label = { Text("최대 인원수") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("대면 여부")
                Switch(
                    checked = viewModel.isOffline,
                    onCheckedChange = { viewModel.isOffline = it })
            }

            OutlinedTextField(
                value = viewModel.minInk,
                onValueChange = { viewModel.minInk = it },
                label = { Text("최소 잉크") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("정기 여부")
                Switch(
                    checked = viewModel.isRegular,
                    onCheckedChange = { viewModel.isRegular = it })
            }
            Spacer(modifier = Modifier.height(12.dp))

            CategorySelector(viewModel.selectedCategories)

            OutlinedTextField(
                value = viewModel.penCount,
                onValueChange = { viewModel.penCount = it },
                label = { Text("만년필 수") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                val currentData = StudyCreationData(
                    name = viewModel.name,
                    startDate = viewModel.startDate,
                    endDate = viewModel.endDate,
                    maxParticipants = viewModel.maxParticipants.toIntOrNull() ?: -1,
                    isOffline = viewModel.isOffline,
                    minInk = viewModel.minInk.toIntOrNull() ?: -1,
                    isRegular = viewModel.isRegular,
                    selectedCategories = viewModel.selectedCategories.toList(),
                    penCount = viewModel.penCount.toIntOrNull() ?: -1
                )

                val errors = validateStudy(currentData)

                if (errors.isEmpty()) {
                    Log.d("StudyEdit", "✅ 유효성 통과: $currentData")
                    // DB 저장 예정
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(errors.first())
                    }
                }
            }) {
                Text("수정 완료")
            }
        }
    }
}