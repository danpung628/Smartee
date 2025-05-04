// feature-studylist/src/main/java/com/example/feature_studylist/viewmodel/AddressViewModel.kt

package com.example.feature_studylist.viewmodel

import android.util.Log
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

    // 시/도 목록 가져오기 (테스트용 더미 데이터 포함)
    fun fetchSidoList() {
        viewModelScope.launch {
            try {
                // API 호출 시도
                val response = RetrofitClient.addressService.getAddressList(
                    confmKey = RetrofitClient.getApiKey()
                )

                // 응답에서 시/도 추출 시도
                try {
                    val sidoList = response.results.juso.map { it.siNm }.distinct()
                    if (sidoList.isNotEmpty()) {
                        _addresses.value = sidoList
                        Log.d("AddressViewModel", "Sido list fetched: $sidoList")
                    } else {
                        // API에서 데이터가 없는 경우 더미 데이터 사용
                        setDummyAddresses()
                    }
                } catch (e: Exception) {
                    Log.e("AddressViewModel", "Error processing response: ${e.message}")
                    // 응답 처리 실패 시 더미 데이터 사용
                    setDummyAddresses()
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "Error fetching address: ${e.message}")
                // API 호출 실패 시 더미 데이터 사용
                setDummyAddresses()
            }
        }
    }

    // 특정 시/도의 구/군 목록 가져오기
    fun fetchSggList(sido: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.addressService.getAddressList(
                    confmKey = RetrofitClient.getApiKey(),
                    keyword = sido  // 키워드로 시/도 이름을 넘겨줌
                )

                try {
                    // 해당 시/도의 구/군 이름만 추출해서 중복 제거
                    val sggList = response.results.juso
                        .filter { it.siNm == sido }
                        .map { it.sggNm }
                        .distinct()

                    if (sggList.isNotEmpty()) {
                        _addresses.value = sggList
                        Log.d("AddressViewModel", "Sgg list fetched for $sido: $sggList")
                    } else {
                        // 데이터가 없는 경우 더미 데이터
                        _addresses.value = getDummySggList(sido)
                    }
                } catch (e: Exception) {
                    Log.e("AddressViewModel", "Error processing sgg response: ${e.message}")
                    _addresses.value = getDummySggList(sido)
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "Error fetching sgg list: ${e.message}")
                _addresses.value = getDummySggList(sido)
            }
        }
    }

    // 최소한의 더미 데이터만 설정 (테스트용)
    private fun setDummyAddresses() {
        val dummySidoList = listOf(
            "서울특별시", "경기도", "API 에러 - 개발자에게 문의"
        )
        _addresses.value = dummySidoList
        Log.d("AddressViewModel", "Using dummy sido list (API ERROR)")
    }

    // 최소한의 더미 구/군 데이터
    private fun getDummySggList(sido: String): List<String> {
        return when (sido) {
            "서울특별시" -> listOf("강남구", "광진구", "API 테스트")
            "경기도" -> listOf("성남시", "수원시", "API 테스트")
            else -> listOf("API 에러 - 개발자에게 문의")
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
                Log.d("AddressViewModel", "Search results: ${response.results.juso.size} items")
            } catch (e: Exception) {
                Log.e("AddressViewModel", "Error searching address: ${e.message}")
            }
        }
    }
}