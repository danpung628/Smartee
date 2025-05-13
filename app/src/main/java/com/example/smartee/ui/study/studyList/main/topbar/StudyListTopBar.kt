package com.example.smartee.ui.study.studyList.main.topbar

import AddressSearchBar
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
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyListTopBar(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel,
    onSelectAddress:(String)->Unit,
    onSearchNavigate: () -> Unit
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AddressSearchBar(onSelectAddress = onSelectAddress)
//        AddressList(
//            onSelectAddress = onSelectAddress,
//            studyViewModel = studyViewModel
//        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier.clickable(
                onClick = onSearchNavigate
            )
        )
    }
}