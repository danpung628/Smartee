package com.example.feature_studylist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature_studylist.uicomponents.topbar.address.api.RetrofitClient
import com.example.feature_studylist.uicomponents.topbar.address.api.checkSearchedWord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddressViewModel : ViewModel() {
    private val _addresses = MutableStateFlow<List<String>>(emptyList())
    val addresses: StateFlow<List<String>> = _addresses

    // 시/도 목록 가져오기
    fun fetchSidoList() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.addressService.getAddressList(
                    confmKey = RetrofitClient.getApiKey()  // serviceKey 대신 confmKey 사용
                )

                // 시/도 이름만 추출해서 중복 제거
                val sidoList = response.results.juso.map { it.siNm }.distinct()
                _addresses.value = sidoList
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // 특정 시/도의 구/군 목록 가져오기
    fun fetchSggList(sido: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.addressService.getAddressList(
                    confmKey = RetrofitClient.getApiKey(),  // serviceKey 대신 confmKey 사용
                    resultType = "json"  // JSON 형식으로 결과 받기
                )

                // 해당 시/도의 구/군 이름만 추출해서 중복 제거
                val sggList = response.results.juso
                    .filter { it.siNm == sido }
                    .map { it.sggNm }
                    .distinct()

                _addresses.value = sggList
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun searchAddress(keyword: String) {
        val filteredKeyword = checkSearchedWord(keyword)

        // 필터링된 키워드로 API 호출
        viewModelScope.launch {
            try {
                val response = RetrofitClient.addressService.getAddressList(
                    confmKey = RetrofitClient.getApiKey(),
                    keyword = filteredKeyword
                )
                // 응답 처리
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}