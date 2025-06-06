package com.example.smartee.ui.study.studyList.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.viewmodel.AddressViewModel
import kotlinx.coroutines.delay

@Composable
fun AddressSearchField(
    addressViewModel: AddressViewModel = viewModel(),
) {
    var isFocused by remember { mutableStateOf(false) }
    var lastSubmittedQuery by remember { mutableStateOf("") }

    Column {
        // 검색창
        OutlinedTextField(
            value = addressViewModel.addressSearchQuery,
            onValueChange = {
                addressViewModel.addressSearchQuery = it
            },
            label = { Text("지역 검색") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = "위치")
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "검색",
                    modifier = Modifier.clickable {
                        if (addressViewModel.addressSearchQuery.isNotEmpty() &&
                            addressViewModel.addressSearchQuery != lastSubmittedQuery) {
                            addressViewModel.searchAddresses(addressViewModel.addressSearchQuery)
                            lastSubmittedQuery = addressViewModel.addressSearchQuery
                        }
                    }
                )
            }
        )

        // Debounce 처리 - 타이핑 멈추면 검색 실행
        LaunchedEffect(addressViewModel.addressSearchQuery) {
            if (addressViewModel.addressSearchQuery.isNotEmpty() &&
                addressViewModel.addressSearchQuery != lastSubmittedQuery) {
                delay(500) // 500ms 딜레이
                addressViewModel.searchAddresses(addressViewModel.addressSearchQuery)
                lastSubmittedQuery = addressViewModel.addressSearchQuery
            }
        }

        // 자동완성 제안 목록 - 포커스가 있거나 결과가 있을 때만 표시
        if (isFocused && addressViewModel.addressSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(addressViewModel.addressSuggestions) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    addressViewModel.addressSearchQuery = suggestion
                                    addressViewModel.addressSuggestions = emptyList()
                                    isFocused = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}