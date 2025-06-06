package com.example.smartee.ui.study.studyList.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartee.model.factory.CategoryListFactory
import com.example.smartee.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySearchBar(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel,
    onSubmit: (String) -> Unit
) {
    val typedText = studyViewModel.typedText
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            query = typedText,
            onQueryChange = { studyViewModel.typedText = it },
            onSearch = {
                studyViewModel.searchKeyword = it
                onSubmit(it)
                studyViewModel.refreshStudyList()
            },
            active = false,
            onActiveChange = { },
            placeholder = { Text("스터디 검색") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                Row {
                    if (typedText.isNotEmpty()) {
                        IconButton(onClick = { studyViewModel.typedText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }

                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { }

        // 필터 섹션
        AnimatedVisibility(visible = showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 선택된 필터 요약
                if (studyViewModel.selectedCategory.isNotEmpty() ||
                    studyViewModel.selectedAddress.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "적용된 필터",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (studyViewModel.selectedAddress.isNotEmpty()) {
                                Text(
                                    "지역: ${studyViewModel.selectedAddress}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (studyViewModel.selectedCategory.isNotEmpty()) {
                                Text(
                                    "카테고리: ${studyViewModel.selectedCategory.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 필터 적용 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            // 필터 초기화
                            studyViewModel.selectedAddress = ""
                            studyViewModel.selectedCategory = CategoryListFactory.makeCategoryList().toList()
                        }
                    ) {
                        Text("초기화")
                    }

                    TextButton(
                        onClick = {
                            // 필터 적용 및 검색
                            studyViewModel.refreshStudyList()
                            showFilters = false
                        }
                    ) {
                        Text("적용")
                    }
                }
            }
        }
    }
}