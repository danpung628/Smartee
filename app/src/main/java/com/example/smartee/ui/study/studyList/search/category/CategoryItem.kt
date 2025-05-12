package com.example.smartee.ui.study.studyList.search.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    category: String,
    onClick: () -> Unit
) {
    Box(
        modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            category,
            modifier = modifier.shadow(10.dp)
            )
    }
}