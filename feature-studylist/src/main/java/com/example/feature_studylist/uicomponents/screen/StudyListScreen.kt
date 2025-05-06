package com.example.smartee.ui.study

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature_studylist.uicomponents.screen.LocalNavGraphViewModelStoreOwner
import com.example.feature_studylist.uicomponents.studylist.StudyListContent
import com.example.feature_studylist.uicomponents.topbar.StudyListTopBar
import com.example.feature_studylist.viewmodel.StudyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun StudyListScreen(
    keyword: String,
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit
) {
    val studyViewModel: StudyViewModel = viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)
    val isRefreshing = studyViewModel.isRefreshing
    val swipeState = rememberSwipeRefreshState(isRefreshing)//새로고침 기능

    var selectedAddress by remember { mutableStateOf("") }

    SwipeRefresh(
        state = swipeState,
        onRefresh = { studyViewModel.refreshStudyList() }
    ) {
        LazyColumn {
            item {
                StudyListTopBar(
                    onSearchNavigate = onSearchNavigate,
                    onSelectAddress =  {
                        selectedAddress = it
                    }
                )
            }
            item {
                StudyListContent(
                    filteredStudyList = studyViewModel.filteredStudyList,
                    onStudyDetailNavigate = onStudyDetailNavigate,
                    selectedAddress = selectedAddress
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