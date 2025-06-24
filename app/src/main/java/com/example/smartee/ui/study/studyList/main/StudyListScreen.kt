package com.example.smartee.ui.study.studyList.main

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
import com.example.smartee.viewmodel.RecommendationViewModel
import com.example.smartee.viewmodel.RecommendationViewModelFactory
import com.example.smartee.viewmodel.StudyViewModel
import com.example.smartee.viewmodel.UserViewModel
import com.example.smartee.viewmodel.UserViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
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
            userViewModel
        )
    )

    // 현재 사용자 ID 가져오기
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.uid ?: ""

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                studyViewModel.refreshStudyList()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(studyViewModel) {
        studyViewModel.onStudiesLoaded = { studies ->
            recommendationViewModel.refreshRecommendation(studies)
        }
        onDispose {
            studyViewModel.onStudiesLoaded = null
        }
    }

    Column(modifier = modifier) {
        StudyListTopBar(onSearchNavigate = onSearchNavigate)

        // ✅ 새로운 Material3 PullToRefresh 사용
        PullToRefreshBox(
            isRefreshing = studyViewModel.isRefreshing,
            onRefresh = { studyViewModel.refreshStudyList() },
            modifier = Modifier.fillMaxSize()
        ) {
            StudyListContent(
                studyViewModel = studyViewModel,
                onStudyDetailNavigate = onStudyDetailNavigate,
                recommendationViewModel = recommendationViewModel,
                currentUserId = currentUserId
            )
        }
    }
}