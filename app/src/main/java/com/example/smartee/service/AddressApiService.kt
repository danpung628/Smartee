package com.example.smartee.service

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

// 새로 만들 클래스
class AddressApiService(context: Context) {
    private val placesClient = Places.createClient(context)

    // API로 주소 검색해서 가져오기
    suspend fun getAddresses(query: String = ""): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Places API 설정 확인 로그 추가
                Log.d("AddressApi", "Places 초기화 상태: ${Places.isInitialized()}")

                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setTypesFilter(listOf(PlaceTypes.LOCALITY, PlaceTypes.SUBLOCALITY))
                    .setCountries("KR") // 한국으로 한정
                    .build()

                suspendCancellableCoroutine { continuation ->
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            if (response.autocompletePredictions.isEmpty()) {
                                continuation.resume(listOf("", "[검색 결과 없음]"))
                            } else {
                                val addresses = listOf("") + response.autocompletePredictions
                                    .map { it.getPrimaryText(null).toString() }
                                continuation.resume(addresses)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddressApi", "주소 검색 실패: ${e.message}", e)
                            continuation.resume(listOf("", "[주소 로드 실패]", "[네트워크를 확인하세요]"))
                        }
                }
            } catch (e: Exception) {
                Log.e("AddressApi", "API 예외 발생: ${e.message}", e)
                listOf("", "[Places API 오류]", "[앱을 재시작하세요]")
            }
        }
    }
}