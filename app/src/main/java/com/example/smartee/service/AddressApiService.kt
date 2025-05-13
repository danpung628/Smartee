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
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setTypesFilter(listOf(PlaceTypes.LOCALITY, PlaceTypes.SUBLOCALITY))
                    .build()

                // API 호출 결과를 코루틴으로 변환
                suspendCancellableCoroutine { continuation ->
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            val addresses = listOf("") + response.autocompletePredictions
                                .map { it.getPrimaryText(null).toString() }
                            continuation.resume(addresses)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddressApi", "주소 검색 실패", e)
                            continuation.resume(listOf("", "서울시", "부산시", "대구시"))
                        }
                }
            } catch (e: Exception) {
                Log.e("AddressApi", "API 오류", e)
                listOf("", "서울시", "부산시", "대구시") // 오류 시 기본값
            }
        }
    }
}