package com.example.smartee.ui.study.studyList.main

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
import com.example.smartee.viewmodel.RecommendationViewModel
import com.example.smartee.viewmodel.RecommendationViewModelFactory
import com.example.smartee.viewmodel.StudyViewModel
import com.example.smartee.viewmodel.UserViewModel
import com.example.smartee.viewmodel.UserViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StudyListScreen(
    modifier: Modifier = Modifier,
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit,
) {
    val studyViewModel: StudyViewModel =
        viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)
    val authViewModel = LocalAuthViewModel.current
    val userViewModel: UserViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current,
        factory = UserViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val recommendationViewModel: RecommendationViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current,
        factory = RecommendationViewModelFactory(
            LocalContext.current.applicationContext as Application,
            authViewModel,
            userViewModel
        )
    )

    DisposableEffect(studyViewModel) {
        studyViewModel.onStudiesLoaded = { studies ->
            recommendationViewModel.refreshRecommendation(studies)
        }
        onDispose {
            studyViewModel.onStudiesLoaded = null
        }
    }

    val swipeState = rememberSwipeRefreshState(studyViewModel.isRefreshing)

    Column(modifier = modifier) {
        StudyListTopBar(onSearchNavigate = onSearchNavigate)
        SwipeRefresh(
            state = swipeState,
            onRefresh = { studyViewModel.refreshStudyList() }
        ) {
            StudyListContent(
                studyViewModel = studyViewModel,
                onStudyDetailNavigate = onStudyDetailNavigate,
                recommendationViewModel = recommendationViewModel,
            )
        }
    }
}