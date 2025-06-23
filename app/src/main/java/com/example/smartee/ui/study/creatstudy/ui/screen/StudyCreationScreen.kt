package com.example.smartee.ui.study.creatstudy.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.common.LoadingOverlay
import com.example.smartee.ui.signup.RegionDropdown
import com.example.smartee.ui.signup.loadRegionData
import com.example.smartee.ui.study.editstudy.ui.component.CategorySelector
import com.example.smartee.ui.study.editstudy.ui.component.DatePickerField
import com.example.smartee.viewmodel.StudyCreationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyCreationScreen(navController: NavController) {
    val viewModel: StudyCreationViewModel = viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)
    val context = LocalContext.current

    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("새 스터디 생성") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CreationSection(title = "기본 정보") {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        label = { Text("스터디 이름") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.fillMaxWidth()) {
                        DatePickerField("시작일", viewModel.startDate, { viewModel.startDate = it }, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        DatePickerField("종료일", viewModel.endDate, { viewModel.endDate = it }, Modifier.weight(1f))
                    }
                    val regionData = remember { loadRegionData(context) }
                    var selectedSido by remember { mutableStateOf("") }
                    var selectedSigungu by remember { mutableStateOf("") }
                    RegionDropdown(
                        regionData = regionData,
                        selectedSido = selectedSido,
                        selectedSigungu = selectedSigungu,
                        onSidoSelected = {
                            selectedSido = it
                            selectedSigungu = ""
                        },
                        onSigunguSelected = {
                            selectedSigungu = it
                            viewModel.address = "$selectedSido $it"
                        }
                    )
                }

                CreationSection(title = "참가 조건") {
                    OutlinedTextField(
                        value = viewModel.maxParticipants,
                        onValueChange = { viewModel.maxParticipants = it },
                        label = { Text("최대 인원수 (0은 무제한)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = viewModel.minInk,
                        onValueChange = { viewModel.minInk = it },
                        label = { Text("최소 잉크") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = viewModel.penCount,
                        onValueChange = { viewModel.penCount = it },
                        label = { Text("소모 만년필") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                CreationSection(title = "진행 방식") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("대면 스터디", modifier = Modifier.weight(1f))
                        Switch(checked = viewModel.isOffline, onCheckedChange = { viewModel.isOffline = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("정기적 스터디", modifier = Modifier.weight(1f))
                        Switch(checked = viewModel.isRegular, onCheckedChange = { viewModel.isRegular = it })
                    }
                }

                CreationSection(title = "카테고리 (최대 5개)") {
                    // [수정] CategorySelector 컴포넌트 재사용
                    CategorySelector(
                        selectedCategories = viewModel.selectedCategories,
                        onCategoryClick = { category ->
                            viewModel.toggleCategory(category)
                        }
                    )
                }

                CreationSection(title = "스터디 소개") {
                    OutlinedTextField(
                        value = viewModel.description,
                        onValueChange = { viewModel.description = it },
                        label = { Text("스터디 소개") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                CreationSection(title = "추가 정보") {
                    OutlinedTextField(
                        value = viewModel.punishment,
                        onValueChange = { viewModel.punishment = it },
                        label = { Text("벌칙 (선택)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        isSaving = true
                        viewModel.submit(
                            onSuccess = { title ->
                                isSaving = false
                                Toast.makeText(context, "'$title' 스터디가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onFailure = { error ->
                                isSaving = false
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isSaving
                ) {
                    Text("생성 완료")
                }
                Spacer(Modifier.height(16.dp))
            }

            if (isSaving) {
                LoadingOverlay()
            }
        }
    }
}

// UI 구조화를 위한 헬퍼 Composable
@Composable
private fun CreationSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Divider()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}