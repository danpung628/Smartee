package com.example.feature_studylist.uicomponents.topbar.address.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    private const val BASE_URL = "https://www.juso.go.kr/addrlink/addrLinkApi.do/"
    internal const val API_KEY = "devU01TX0FVVEgyMDI1MDUwNTAwMDY1MzExNTcxOTI="

    // API_KEY를 반환하는 함수 추가
    fun getApiKey(): String = API_KEY

    val addressService: AddressService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AddressService::class.java)
    }
}