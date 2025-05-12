package com.example.smartee.ui.study

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.StudyListContent
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
import com.example.smartee.viewmodel.StudyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StudyListScreen(
    keyword: String,
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit
) {
    val studyViewModel: StudyViewModel =
        viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)
    val isRefreshing = studyViewModel.isRefreshing
    val swipeState = rememberSwipeRefreshState(isRefreshing)//새로고침 기능

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
                filteredStudyList = studyViewModel.filteredStudyList,
                onStudyDetailNavigate = onStudyDetailNavigate,
            )
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