package com.example.smartee.ui.study

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.feature_studylist.uicomponents.StudyList
import com.example.feature_studylist.viewmodel.StudyViewModel

@Composable
fun StudyListScreen(
    studyViewModel: StudyViewModel = viewModel()
) {
    val studyList = studyViewModel.studyList
    LazyColumn {
        item {
            Text("Study List Screen")
        }
        item {
            StudyList(
                studyList = studyList
            )
        }
    }
}

@Preview
@Composable
private fun StudyListScreenPreview() {
    StudyListScreen();
}