package com.example.smartee.ui.study.studyList.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.model.StudyData
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.main.topbar.StudyListTopBar
import com.example.smartee.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StudyListScreen(
    onStudyDetailNavigate: (String) -> Unit,
    onSearchNavigate: () -> Unit
) {
    val studyViewModel: StudyViewModel =
        viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)

    // LiveData를 Compose 상태로 변환
    val filteredStudyList = studyViewModel.filteredStudyList.observeAsState(initial = emptyList<StudyData>()).value

//    val isRefreshing = studyViewModel.isRefreshing

//val swipeState = rememberSwipeRefreshState(isRefreshing)//새로고침 기능
//    SwipeRefresh(
//        state = swipeState,
//        onRefresh = { studyViewModel.refreshStudyList() }
//    ) {
//        Column {
//            StudyListTopBar(
//                onSearchNavigate = onSearchNavigate,
//                onSelectAddress = {
//                    studyViewModel.selectedAddress = it
//                },
//                studyViewModel = studyViewModel
//            )
//            StudyListContent(
//                filteredStudyList = studyViewModel.filteredStudyList,
//                filteredStudyList = filteredStudyList,
//                onStudyDetailNavigate = onStudyDetailNavigate,
//            )
//        }
//    }

//    val pullRefreshState = rememberPullRefreshState(
//        refreshing = isRefreshing,
//        onRefresh = { studyViewModel.refreshStudyList() }
//    )
//    Box(Modifier.pullRefresh(pullRefreshState)) {
        Column {
            StudyListTopBar(
                onSearchNavigate = onSearchNavigate,
                onSelectAddress = {
                    studyViewModel.selectedAddress = it
                },
                studyViewModel = studyViewModel
            )
            StudyListContent(
                filteredStudyList = filteredStudyList,
                onStudyDetailNavigate = onStudyDetailNavigate,
            )
        }
//        PullRefreshIndicator(
//            refreshing = isRefreshing,
//            state = pullRefreshState,
//            modifier = Modifier.align(Alignment.TopCenter)
//        )
//    }

}

@Preview
@Composable
private fun StudyListScreenPreview() {
//    StudyListScreen(keyword = "") {
//
//    }
}