package com.example.smartee.ui.study.studyList.studydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudyCategoryChips(categoryString: String) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        maxItemsInEachRow = 3
    ) {
        categoryString.split(",").forEach { category ->
            SuggestionChip(
                onClick = { },
                label = { Text(category.trim()) },
                modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
            )
        }
    }
}