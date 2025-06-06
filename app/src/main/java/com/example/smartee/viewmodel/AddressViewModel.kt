package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.service.AddressApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddressViewModel(app: Application) : AndroidViewModel(app) {
    private val addressApiService = AddressApiService(app)

    // 상태 관리
    var addressSuggestions by mutableStateOf<List<String>>(emptyList())
    var addressSearchQuery by mutableStateOf("")

    // 디바운스 처리를 위한 Job
    private var searchJob: Job? = null

    // 주소 검색 기능 (디바운스 처리)
    fun searchAddresses(query: String) {
        // 이전 검색 작업 취소
        searchJob?.cancel()

        if (query.length < 2) {
            addressSuggestions = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            try {
                Log.d("AddressViewModel", "주소 검색 요청: $query")
                val results = addressApiService.searchAddresses(query)
                addressSuggestions = results
                Log.d("AddressViewModel", "검색 결과: ${results.size}개")
            } catch (e: Exception) {
                Log.e("AddressViewModel", "주소 검색 실패", e)
                addressSuggestions = emptyList()
            }
        }
    }
}