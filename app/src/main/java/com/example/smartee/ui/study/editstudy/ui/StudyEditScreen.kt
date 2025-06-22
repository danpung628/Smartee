package com.example.smartee.ui.study.editstudy.ui
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.viewmodel.StudyEditViewModel
import com.example.smartee.ui.study.editstudy.util.validateStudy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.smartee.ui.study.editstudy.ui.component.CategorySelector
import com.example.smartee.ui.study.editstudy.ui.component.DatePickerField

@Composable
fun StudyEditScreen(studyId: String,
                    vm: StudyEditViewModel = viewModel())
{

    LaunchedEffect(studyId) {
        vm.loadStudyFromFirebase(studyId)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {
            OutlinedTextField(
                value = vm.title,
                onValueChange = { vm.title = it },
                label = { Text("스터디 이름") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            DatePickerField("시작일", vm.startDate) { vm.startDate = it }
            DatePickerField("종료일", vm.endDate) { vm.endDate = it }

            OutlinedTextField(
                value = vm.maxMemberCount,
                onValueChange = { vm.maxMemberCount = it },
                label = { Text("최대 인원수") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("대면 여부")
                Switch(
                    checked = vm.isOffline,
                    onCheckedChange = { vm.isOffline = it })
            }

            OutlinedTextField(
                value = vm.minInkLevel,
                onValueChange = { vm.minInkLevel = it },
                label = { Text("최소 잉크") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("정기 여부")
                Switch(
                    checked = vm.isRegular,
                    onCheckedChange = { vm.isRegular = it })
            }
            Spacer(modifier = Modifier.height(12.dp))

            CategorySelector(vm.selectedCategories)

            OutlinedTextField(
                value = vm.penCount,
                onValueChange = { vm.penCount = it },
                label = { Text("만년필 수") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                val studyToSave = vm.toStudyData()
                val errors = validateStudy(studyToSave)

                if (errors.isEmpty()) {
                    val db = FirebaseFirestore.getInstance()
                    val docId = vm.studyId.ifEmpty {
                        db.collection("studies").document().id.also {
                            vm.studyId = it // 새로 생성한 ID 저장
                        }
                    }

                    db.collection("studies").document(docId)
                        .set(studyToSave.copy(studyId = docId))
                        .addOnSuccessListener {
                            Log.d("StudyEdit", "✅ 수정된 스터디 저장 성공")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("수정 완료되었습니다!")
                            }
                        }
                        .addOnFailureListener {
                            Log.e("StudyEdit", "❌ 저장 실패", it)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("저장 실패: ${it.message}")
                            }
                        }
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