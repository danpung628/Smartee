package com.example.feature_studylist.uicomponents.topbar.address.api

data class AddressItem(
    val roadAddr: String,     // 도로명주소
    val jibunAddr: String,    // 지번주소
    val zipNo: String,        // 우편번호
    val siNm: String,         // 시도명
    val sggNm: String,        // 시군구명
    val emdNm: String,        // 읍면동명
    val liNm: String          // 리명
)