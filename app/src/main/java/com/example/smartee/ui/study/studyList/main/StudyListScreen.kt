package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
import com.example.smartee.viewmodel.RecommendationViewModel
import com.example.smartee.viewmodel.StudyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudyListScreen(
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit
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


    val swipeState = rememberSwipeRefreshState(studyViewModel.isRefreshing)//새로고침 기능
    SwipeRefresh(
        state = swipeState,
        onRefresh = { studyViewModel.refreshStudyList() }
    ) {
        Column {
            StudyListTopBar(
                onSearchNavigate = onSearchNavigate,
            )
            StudyListContent(
                studyViewModel = studyViewModel,
                onStudyDetailNavigate = onStudyDetailNavigate,
            )
        }
    }

}