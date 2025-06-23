package com.example.smartee.ui.study.editstudy.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.model.StudyData
import com.example.smartee.ui.common.LoadingOverlay
import com.example.smartee.ui.study.editstudy.ui.component.CategorySelector
import com.example.smartee.ui.study.editstudy.ui.component.DatePickerField
import com.example.smartee.ui.study.editstudy.util.validateStudy
import com.example.smartee.viewmodel.StudyEditViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// [수정] NavController를 파라미터로 받아 뒤로가기 기능을 구현합니다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyEditScreen(
    studyId: String,
    navController: NavController, // [추가]
    vm: StudyEditViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 데이터 로딩
    LaunchedEffect(studyId) {
        isLoading = true
        vm.loadStudyFromFirebase(studyId, onComplete = { isLoading = false })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // [추가] TopAppBar를 추가하여 화면 제목과 뒤로가기 버튼을 만듭니다.
        topBar = {
            TopAppBar(
                title = { Text("스터디 수정") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // [수정] 콘텐츠 영역을 Column으로 감싸고 스크롤 및 정렬 옵션을 추가합니다.
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()) // [추가]
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // [추가]
            ) {
                // 기본 정보 섹션
                EditSection(title = "기본 정보") {
                    OutlinedTextField(
                        value = vm.title,
                        onValueChange = { vm.title = it },
                        label = { Text("스터디 이름") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.fillMaxWidth()) {
                        DatePickerField("시작일", vm.startDate, { vm.startDate = it }, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        DatePickerField("종료일", vm.endDate, { vm.endDate = it }, Modifier.weight(1f))
                    }
                }

                // 참가 조건 섹션
                EditSection(title = "참가 조건") {
                    OutlinedTextField(
                        value = vm.maxMemberCount,
                        onValueChange = { vm.maxMemberCount = it },
                        label = { Text("최대 인원수 (0은 무제한)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vm.minInkLevel,
                        onValueChange = { vm.minInkLevel = it },
                        label = { Text("최소 잉크") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vm.penCount,
                        onValueChange = { vm.penCount = it },
                        label = { Text("소모 만년필") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 진행 방식 섹션
                EditSection(title = "진행 방식") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("대면 여부", modifier = Modifier.weight(1f))
                        Switch(checked = vm.isOffline, onCheckedChange = { vm.isOffline = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("정기 여부", modifier = Modifier.weight(1f))
                        Switch(checked = vm.isRegular, onCheckedChange = { vm.isRegular = it })
                    }
                }

                // 카테고리 섹션
                EditSection(title = "카테고리 (최대 5개)") {
                    CategorySelector(
                        selectedCategories = vm.selectedCategories,
                        // [수정] ViewModel의 토글 함수를 넘겨줍니다.
                        onCategoryClick = { category ->
                            vm.toggleCategory(category)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 수정 완료 버튼
                Button(
                    onClick = {
                        // [수정] 유효성 검사를 위해 임시 StudyData 객체를 만듭니다.
                        // (기존 toStudyData()의 반환 타입이 Map으로 바뀌었기 때문)
                        val tempStudyDataForValidation = vm.toStudyData().let { map ->
                            StudyData(
                                title = map["title"] as String,
                                startDate = map["startDate"] as String,
                                endDate = map["endDate"] as String,
                                maxMemberCount = map["maxMemberCount"] as Int,
                                minInkLevel = map["minInkLevel"] as Int,
                                penCount = map["penCount"] as Int,
                                category = map["category"] as String
                            )
                        }
                        val errors = validateStudy(tempStudyDataForValidation)

                        if (errors.isEmpty()) {
                            isSaving = true
                            val db = FirebaseFirestore.getInstance()

                            // [수정] ViewModel에서 업데이트할 데이터 맵을 가져옵니다.
                            val studyUpdateMap = vm.toStudyData()

                            db.collection("studies").document(vm.studyId)
                                .update(studyUpdateMap) // [중요] .set() 대신 .update()를 사용합니다.
                                .addOnSuccessListener {
                                    Log.d("StudyEdit", "✅ 수정된 스터디 저장 성공")
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("수정이 완료되었습니다!")
                                        navController.popBackStack()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("StudyEdit", "❌ 저장 실패", e)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("저장 실패: ${e.message}") }
                                }
                                .addOnCompleteListener { isSaving = false }
                        } else {
                            coroutineScope.launch { snackbarHostState.showSnackbar(errors.first()) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isSaving
                ) {
                    Text("수정 완료")
                }
                Spacer(modifier = Modifier.height(16.dp)) // 버튼 하단 여백
            }

            // [추가] 로딩 오버레이
            if (isLoading || isSaving) {
                LoadingOverlay()
            }
        }
    }
}


// [추가] UI 구조화를 위한 헬퍼 Composable
@Composable
private fun EditSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Divider()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}