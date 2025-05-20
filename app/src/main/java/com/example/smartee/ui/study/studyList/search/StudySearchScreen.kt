package com.example.smartee.ui.study.studyList.search

import AddressSearchField
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    Column {
        StudySearchBar(
            studyViewModel = studyViewModel,
        ) {
            studyViewModel.selectedAddress = addressViewModel.addressSearchQuery
            onSubmitNavigate(it)
        }
        AddressSearchField(
            addressViewModel = addressViewModel
        )
        StudyCategory(studyViewModel = studyViewModel)
    }
}

@Preview
@Composable
fun StudySearchScreenPreview() {
    StudySearchScreen {

    }
}