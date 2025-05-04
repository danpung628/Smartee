package com.example.smartee.ui.study

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature_studylist.uicomponents.studylist.StudyListContent
import com.example.feature_studylist.uicomponents.topbar.StudyListTopBar
import com.example.feature_studylist.viewmodel.StudyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StudyListScreen(
    studyViewModel: StudyViewModel = viewModel(),
    keyword: String,
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit
) {
    val studyList = studyViewModel.studyList

    val isRefreshing = studyViewModel.isRefreshing
    val swipeState = rememberSwipeRefreshState(isRefreshing)//새로고침 기능

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
                StudyListContent(
                    studyList = studyList,
                    keyword = keyword,
                    onStudyDetailNavigate = onStudyDetailNavigate
                )
            }
        }
    }
}

@Preview
@Composable
private fun StudyListScreenPreview() {
//    StudyListScreen(keyword = "") {
//
//    }
}