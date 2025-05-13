package com.example.smartee.ui.study.studyList.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudySearchBar(
    modifier: Modifier,
    studyViewModel: StudyViewModel,
    onSubmitNavigate: (String) -> Unit
) {
    var typedText = studyViewModel.typedText

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = typedText,
                onValueChange = {
                    studyViewModel.typedText = it
                },
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Submit",
                modifier = Modifier
                    .clickable {
                        studyViewModel.searchKeyword = typedText
                        studyViewModel.refreshStudyList()
                        onSubmitNavigate(typedText)
                    }
            )
        }
    }
}