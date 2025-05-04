package com.example.feature_studylist.uicomponents.topbar.address.api

data class CommonResponse(
    val totalCount: Int,
    val currentPage: Int,
    val errorCode: String,
    val errorMessage: String
)