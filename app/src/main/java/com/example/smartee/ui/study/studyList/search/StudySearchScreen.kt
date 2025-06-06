package com.example.smartee.ui.study.studyList.search

import AddressSearchField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.search.category.StudyCategory
import com.example.smartee.viewmodel.AddressViewModel
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudySearchScreen(
    modifier: Modifier = Modifier,
    onSubmitNavigate: (String) -> Unit
) {
    val studyViewModel: StudyViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current
    )
    val addressViewModel: AddressViewModel =
        viewModel(viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 메인 검색바
        StudySearchBar(
            studyViewModel = studyViewModel,
            onSubmit = {
                studyViewModel.selectedAddress = addressViewModel.addressSearchQuery
                onSubmitNavigate(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // 주소 검색 필드
        AddressSearchField(
            addressViewModel = addressViewModel
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // 카테고리 선택
        StudyCategory(studyViewModel = studyViewModel)
    }
}