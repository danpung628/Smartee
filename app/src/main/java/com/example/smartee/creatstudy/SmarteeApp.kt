package com.example.smartee.creatstudy

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.screen.StudyCreationScreen
import com.example.smartee.creatstudy.viewmodel.StudyCreationViewModel

@Composable
fun SmarteeApp() {
    val viewModel: StudyCreationViewModel = viewModel()
    StudyCreationScreen(viewModel)
}