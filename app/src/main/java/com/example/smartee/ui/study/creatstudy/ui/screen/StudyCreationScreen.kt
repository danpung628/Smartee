package com.example.smartee.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.component.DatePickerField
import com.example.smartee.viewmodel.StudyCreationViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudyCreationScreen(navController: NavController) {
    val viewModel: StudyCreationViewModel =
        viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)

    val context = LocalContext.current
    val categories = listOf("CS", "자격증", "코딩", "운동", "토익", "면접", "영어", "자기계발", "기타")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.title = it }, label = { Text("스터디 이름") })
        Spacer(modifier = Modifier.height(16.dp))

        DatePickerField("시작일", viewModel.startDate) { viewModel.startDate = it }
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerField("종료일", viewModel.endDate) { viewModel.endDate = it }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = viewModel.maxParticipants, onValueChange = { viewModel.maxParticipants = it }, label = { Text("최대 인원수") })
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = viewModel.minInk, onValueChange = { viewModel.minInk = it }, label = { Text("최소 잉크") })
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = viewModel.penCount, onValueChange = { viewModel.penCount = it }, label = { Text("만년필 수") })
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = viewModel.isOffline, onCheckedChange = { viewModel.isOffline = it })
            Text("대면 스터디")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = viewModel.isRegular, onCheckedChange = { viewModel.isRegular = it })
            Text("정기적 스터디")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("카테고리 선택", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // LazyVerticalGrid requires Experimental
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val selected = viewModel.selectedCategories.contains(category)

                Button(
                    onClick = { viewModel.toggleCategory(category) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) Color.Gray else Color.LightGray
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                ) {
                    Text(category)
                }
            }
        }


        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(value = viewModel.punishment, onValueChange = { viewModel.punishment = it }, label = { Text("벌칙 (선택)") })
        Spacer(modifier = Modifier.height(24.dp))

        viewModel.errorMessage?.let {
            Text(it, color = Color.Red)
        }

        Button(onClick = {
            viewModel.submit()
            if (viewModel.errorMessage == null) {
                viewModel.submittedStudies.lastOrNull()?.let {
                    Toast.makeText(context, "제출 완료: ${it.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("완료")
        }
    }
}
