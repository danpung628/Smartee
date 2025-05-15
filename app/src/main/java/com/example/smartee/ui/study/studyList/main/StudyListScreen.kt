package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
import com.example.smartee.viewmodel.RecommendationViewModel
import com.example.smartee.viewmodel.StudyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StudyListScreen(
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit,
    onStudyCreateNavigate: () -> Unit,
    onProfileNavigate: () -> Unit,
    onHomeNavigate: () -> Unit
) {
    val studyViewModel: StudyViewModel =
        viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)
    val recommendationViewModel: RecommendationViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current
    )

    // 스터디 목록 로드될 때 추천 요청하도록 설정
    DisposableEffect(studyViewModel) {
        studyViewModel.onStudiesLoaded = { studies ->
            recommendationViewModel.refreshRecommendation(studies)
        }

        onDispose {
            studyViewModel.onStudiesLoaded = null
        }
    }

    val swipeState = rememberSwipeRefreshState(studyViewModel.isRefreshing)

    Scaffold(
        topBar = {
            StudyListTopBar(onSearchNavigate = onSearchNavigate)
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = onHomeNavigate,
                    icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
                    label = { Text("홈") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.List, contentDescription = "내 모임") },
                    label = { Text("내 모임") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileNavigate,
                    icon = { Icon(Icons.Default.Person, contentDescription = "프로필") },
                    label = { Text("프로필") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStudyCreateNavigate) {
                Icon(Icons.Default.Add, contentDescription = "스터디 생성")
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeState,
            onRefresh = { studyViewModel.refreshStudyList() },
            modifier = Modifier.padding(paddingValues)
        ) {
            StudyListContent(
                studyViewModel = studyViewModel,
                onStudyDetailNavigate = onStudyDetailNavigate,
                recommendationViewModel = recommendationViewModel,
            )
        }
    }
}