package com.example.smartee.ui.study

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature_studylist.uicomponents.StudyList
import com.example.feature_studylist.uicomponents.StudyListTopBar
import com.example.feature_studylist.viewmodel.StudyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StudyListScreen(
    studyViewModel: StudyViewModel = viewModel(),
    onSearchNavigate: () -> Unit
) {
    val studyList = studyViewModel.studyList

    val isRefreshing = studyViewModel.isRefreshing
    val swipeState = rememberSwipeRefreshState(isRefreshing)

    SwipeRefresh(
        state = swipeState,
        onRefresh = { studyViewModel.refreshStudyList() }
    ) {
        LazyColumn {
            item {
                StudyListTopBar(
                    onSearchNavigate = onSearchNavigate
                )
            }
            item {
                StudyList(
                    studyList = studyList
                )
            }
        }
    }
}

@Preview
@Composable
private fun StudyListScreenPreview() {
    StudyListScreen {

    }
}