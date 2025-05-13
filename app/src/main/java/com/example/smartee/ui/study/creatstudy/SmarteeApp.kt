package com.example.smartee.ui.study.creatstudy

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.screen.StudyCreationScreen
import com.example.smartee.ui.study.creatstudy.viewmodel.StudyCreationViewModel

@Composable
fun SmarteeApp() {
    val viewModel: StudyCreationViewModel = viewModel()
    StudyCreationScreen(viewModel)
}