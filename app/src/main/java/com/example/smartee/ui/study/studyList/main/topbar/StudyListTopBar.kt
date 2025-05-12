package com.example.smartee.ui.study.studyList.main.topbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartee.ui.study.studyList.main.topbar.address.AddressList
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyListTopBar(
    modifier: Modifier = Modifier.Companion,
    studyViewModel: StudyViewModel,
    onSelectAddress:(String)->Unit,
    onSearchNavigate: () -> Unit
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AddressList(
            onSelectAddress = onSelectAddress,
            studyViewModel = studyViewModel
        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier.clickable(
                onClick = onSearchNavigate
            )
        )
    }
}

@Preview
@Composable
private fun StudyListTopBarPreview() {
//    StudyListTopBar{
//
//    }
}