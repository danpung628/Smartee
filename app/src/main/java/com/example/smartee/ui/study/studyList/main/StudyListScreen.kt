package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
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

val swipeState = rememberSwipeRefreshState(studyViewModel.isRefreshing)//새로고침 기능
    SwipeRefresh(
        state = swipeState,
        onRefresh = { studyViewModel.refreshStudyList() }
    ) {
        Column {
            StudyListTopBar(
                onSearchNavigate = onSearchNavigate,
                onSelectAddress = {
                    studyViewModel.selectedAddress = it
                },
                studyViewModel = studyViewModel
            )
            StudyListContent(
                studyViewModel = studyViewModel,
                onStudyDetailNavigate = onStudyDetailNavigate,
            )
        }
    }

}