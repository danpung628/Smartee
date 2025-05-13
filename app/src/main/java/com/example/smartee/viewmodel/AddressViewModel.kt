package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.service.AddressApiService
import kotlinx.coroutines.launch

class AddressViewModel(app: Application) : AndroidViewModel(app) {
    private val addressApiService = AddressApiService(app)

    // 상태 관리
    var addressSuggestions by mutableStateOf<List<String>>(emptyList())
    var addressSearchQuery by mutableStateOf("")

    // 주소 검색 기능
    fun searchAddresses(query: String) {
        viewModelScope.launch {
            try {
//                if (query.length >= 2) {
                    val results = addressApiService.searchAddresses(query)
                    addressSuggestions = results
                    Log.d("AddressViewModel", "검색 결과: ${results.size}개")
//                } else {
//                    addressSuggestions = emptyList()
//                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "주소 검색 실패", e)
                addressSuggestions = emptyList()
            }
        }
    }
}