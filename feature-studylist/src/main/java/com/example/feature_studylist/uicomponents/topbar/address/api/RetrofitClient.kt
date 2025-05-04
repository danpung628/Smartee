package com.example.feature_studylist.uicomponents.topbar.address.api

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // BASE_URL 수정 - 마지막 슬래시와 경로 수정
    private const val BASE_URL = "https://business.juso.go.kr/addrlink/"
    internal const val API_KEY = "devU01TX0FVVEgyMDI1MDUwNTAwMDY1MzExNTcxOTI="

    // API_KEY를 반환하는 함수
    fun getApiKey(): String = API_KEY

    val addressService: AddressService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AddressService::class.java)
    }
}