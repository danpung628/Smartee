package com.example.feature_studylist.uicomponents.screen

import android.R.attr.text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature_studylist.viewmodel.StudyViewModel

@Composable
fun StudySearchScreen(
    modifier: Modifier = Modifier,
    onSubmitNavigate: (String) -> Unit
) {
    val studyViewModel: StudyViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current
    )
    var typedText = studyViewModel.typedText

    Column(
        modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Submit",
                modifier = Modifier
                    .weight(0.1f)
                    .clickable {
                        studyViewModel.searchKeyword = typedText
                        onSubmitNavigate(typedText)
                    }
            )
        }
    }
}

@Preview
@Composable
fun StudySearchScreenPreview() {
    StudySearchScreen {

    }
}