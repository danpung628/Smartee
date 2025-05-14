package com.example.smartee.service

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AddressApiService(private val context: Context) {
    private val placesClient = Places.createClient(context)
    private val sessionToken = AutocompleteSessionToken.newInstance()

    // API로 주소 검색해서 가져오기
    suspend fun searchAddresses(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AddressApi", "검색 쿼리: $query")

                val request = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.REGIONS)
                    .setSessionToken(sessionToken)
                    .setQuery(query)
                    .setCountries("KR")
                    .build()

                suspendCancellableCoroutine { continuation ->
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            val addresses = response.autocompletePredictions.map {
                                it.getPrimaryText(null).toString()
                            }
                            Log.d("AddressApi", "검색 결과: ${addresses.size}개")
                            continuation.resume(addresses)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddressApi", "주소 검색 실패: ${e.message}", e)
                            continuation.resume(emptyList())
                        }
                }
            } catch (e: Exception) {
                Log.e("AddressApi", "API 예외 발생: ${e.message}", e)
                emptyList()
            }
        }
    }
}