package com.example.smartee.ui.study.studyList.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.study.studyList.search.category.StudyCategory
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudySearchScreen(
    modifier: Modifier = Modifier,
    onSubmitNavigate: (String) -> Unit
) {
    val studyViewModel: StudyViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current
    )
    Column {
        StudySearchBar(
            modifier,
            studyViewModel = studyViewModel,
            onSubmitNavigate = onSubmitNavigate
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