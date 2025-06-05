package com.example.smartee.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.model.StudyData
import com.example.smartee.viewmodel.MyStudyViewModel

@Composable
fun MyStudyScreen(
    viewModel: MyStudyViewModel = viewModel()
) {
    val myCreatedStudies by viewModel.myCreatedStudies.collectAsState()
    val myJoinedStudies by viewModel.myJoinedStudies.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyStudies()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "내가 만든 스터디",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        StudyList(studies = myCreatedStudies)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "내가 참여 중인 스터디",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        StudyList(studies = myJoinedStudies)
    }
}

@Composable
fun StudyList(studies: List<StudyData>) {
    LazyColumn {
        items(studies) { study ->
            StudyListItem(study = study)
        }
    }
}

@Composable
fun StudyListItem(study: StudyData) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Text(text = study.title, style = MaterialTheme.typography.titleSmall)
        Text(text = "카테고리: ${study.category}", style = MaterialTheme.typography.bodySmall)
    }
}
