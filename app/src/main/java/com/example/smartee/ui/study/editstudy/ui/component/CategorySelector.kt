package com.example.smartee.ui.study.editstudy.ui.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartee.model.factory.CategoryListFactory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(selectedCategories: SnapshotStateList<String>) {
//    val allCategories = listOf("스터디", "운동", "영어", "자격증", "취미")
    val allCategories = CategoryListFactory.makeCategoryList()

    FlowRow {
        allCategories.forEach { category ->
            val isSelected = selectedCategories.contains(category)
            Button(
                onClick = {
                    if (isSelected) selectedCategories.remove(category)
                    else selectedCategories.add(category)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(category)
            }
        }
    }
}
