package com.example.feature_studylist.uicomponents.topbar.address.api

data class AddressResults(
    val common: CommonResponse,
    val juso: List<AddressItem>
)