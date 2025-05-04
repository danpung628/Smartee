package com.example.feature_studylist.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.feature_studylist.navGraph.NaviGraph

@Composable
fun StudyListMainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NaviGraph(navController = navController)
}