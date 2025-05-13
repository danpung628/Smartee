package com.example.smartee.ui.study.studyList.search.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle


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
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(4f,4f),
                    blurRadius = 8f
                )
            )
        )
    }
}