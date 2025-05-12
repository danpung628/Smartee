package com.example.smartee.ui.study.studyList.search.category

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.smartee.model.factory.CategoryListFactory
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyCategory(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel
) {
    val categoryList = CategoryListFactory.makeCategoryList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
    ) {
        items(categoryList) {
            CategoryItem(
                Modifier.background(
                    if (it in studyViewModel.selectedCategory) {
                        Color.Green
                    } else {
                        Color.Gray
                    }
                ),
                category = it
            ) {
                studyViewModel.toggleCategory(it)
            }
        }
    }
}