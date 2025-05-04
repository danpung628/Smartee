package com.example.feature_studylist.uicomponents.topbar.address.api

import retrofit2.http.GET
import retrofit2.http.Query

interface AddressService {
    @GET("addrLinkApi.do")
    suspend fun getAddressList(
        @Query("confmKey") confmKey: String,  // serviceKey 대신 confmKey 사용
        @Query("currentPage") page: Int = 1,
        @Query("countPerPage") countPerPage: Int = 100,
        @Query("keyword") keyword: String = "",
        @Query("resultType") resultType: String = "json"
    ): AddressResponse
}