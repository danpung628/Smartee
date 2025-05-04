package com.example.feature_studylist.uicomponents.topbar

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

@Composable
fun StudyListTopBar(
    modifier: Modifier = Modifier,
    onSearchNavigate: () -> Unit
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AddressList()
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
    StudyListTopBar{

    }
}