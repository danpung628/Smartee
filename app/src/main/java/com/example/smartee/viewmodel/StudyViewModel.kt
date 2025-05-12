package com.example.smartee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.model.factory.AddressListFactory
import com.example.smartee.model.factory.CategoryListFactory
import com.example.smartee.model.factory.StudyListFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel : ViewModel() {
    //스터디 목록
    private val _studyList = StudyListFactory.makeStudyList()
    val studyList: MutableList<StudyData>
        get() = _studyList

    //주소 목록
    private val _addressList = AddressListFactory.makeAddressList()
    val addressList: MutableList<String>
        get() = _addressList

    //주소 드롭다운 확장 여부
    var addressExpanded by mutableStateOf(false)

    //드롭다운에서 선택한 주소
    var selectedAddress by mutableStateOf("")

    //스터디 검색창에 현재 입력한 텍스트
    var typedText by mutableStateOf("")

    //실제로 검색할 스터디 키워드
    var searchKeyword by mutableStateOf("")

    //카테고리 체크
    var selectedCategory by mutableStateOf(CategoryListFactory.makeCategoryList().toList())
    fun toggleCategory(category: String) {
        selectedCategory = if (category in selectedCategory) {
            selectedCategory - category
        } else{
            selectedCategory + category
        }
    }

    //주소, 검색 키워드에 따른 필터링
    val filteredStudyList: MutableList<StudyData>
        get() = _studyList.filter {
            it.title.contains(searchKeyword) && it.address.contains(selectedAddress)
        }.toMutableList()

    //새로 고침 동작
    var isRefreshing by mutableStateOf(false)
        private set

    fun refreshStudyList() {
        // 강제로 약간의 지연을 줘야 애니메이션이 보임
        viewModelScope.launch {
            isRefreshing = true

            val newList = _studyList.map {
                it.copy()
            }
            _studyList.clear()
            _studyList.addAll(newList)

            delay(500) // 애니메이션만 보여주고 아무것도 안 함
            isRefreshing = false
        }
    }
}