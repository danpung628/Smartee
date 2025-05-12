package com.example.smartee.model.factory

import androidx.compose.runtime.mutableStateListOf
import com.example.smartee.model.StudyData

object StudyListFactory {
    fun makeStudyList(count: Int = 20): MutableList<StudyData> {
        val categories = CategoryListFactory.makeCategoryList()
        val addresses = AddressListFactory.makeAddressList().filter { it.isNotEmpty() }
        val titles = listOf("영어 스터디", "C++ 스터디", "풋살 모임", "독서 모임", "취업 준비", "코딩 테스트", "자바 스터디")

        return mutableStateListOf<StudyData>().apply {
            repeat(count) { index ->
                add(
                    StudyData(
                        studyId = index.toString(),
                        category = categories.random(),
                        title = titles.random(),
                        address = addresses.random(),
                        maxMemberCount = (2..10).random(),
                        commentCount = (0..100).random(),
                        likeCount = (0..100).random(),
                        thumbnailModel = "https://picsum.photos/200/300?random=$index"
                    )
                )
            }
        }
    }
}