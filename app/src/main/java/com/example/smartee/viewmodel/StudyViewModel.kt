package com.example.smartee.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.model.factory.AddressListFactory
import com.example.smartee.model.factory.CategoryListFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class StudyViewModel : ViewModel() {
    // Firestore 참조
    private val db = FirebaseFirestore.getInstance()
    private val studyCollectionRef = db.collection("studies")
    private val _studyList = MutableLiveData<MutableList<StudyData>>(mutableListOf())
    val studyList: LiveData<MutableList<StudyData>> get() = _studyList
    // 필터링된 스터디 목록도 LiveData로 변경
    val filteredStudyList = studyList.map { list ->
        list.filter {
            it.title.contains(searchKeyword) &&
                    it.address.contains(selectedAddress) &&
                    it.category in selectedCategory
        }.toMutableList()
    }
    // 초기화 시 Firebase에서 데이터 불러오기
    init {
        loadStudiesFromFirebase()
    }
    // Firebase에서 스터디 목록 불러오기
    private fun loadStudiesFromFirebase() {
        viewModelScope.launch {
            isRefreshing = true

            studyCollectionRef.get()
                .addOnSuccessListener { documents ->
                    val studyList = mutableListOf<StudyData>()

                    for (document in documents) {
                        // Firestore 문서에서 StudyData 객체로 변환
                        val study = document.toObject(StudyData::class.java)
                            .copy(studyId = document.id)

                        studyList.add(study)
                    }

                    _studyList.value = studyList
                    isRefreshing = false
                }
                .addOnFailureListener { e ->
                    // 오류 처리
                    Log.e("StudyViewModel", "Error loading studies", e)
                    isRefreshing = false
                }
        }
    }
    // 새로고침 시 Firebase에서 다시 불러오기
    fun refreshStudyList() {
        loadStudiesFromFirebase()
    }
    // 새 스터디 추가
    fun addStudy(study: StudyData) {
        viewModelScope.launch {
            // ID가 없는 새 문서 추가
            studyCollectionRef.add(study)
                .addOnSuccessListener { documentReference ->
                    // 성공 시 로컬 목록에도 추가
                    val newStudy = study.copy(studyId = documentReference.id)
                    val currentList = _studyList.value ?: mutableListOf()
                    currentList.add(newStudy)
                    _studyList.value = currentList
                }
        }
    }

//    //스터디 목록
//    private val _studyList = StudyListFactory.makeStudyList()
//    val studyList: MutableList<StudyData>
//        get() = _studyList

//    //주소, 검색 키워드에 따른 필터링
//    val filteredStudyList: MutableList<StudyData>
//        get() = _studyList.filter {
//            it.title.contains(searchKeyword) && it.address.contains(selectedAddress) && it.category in selectedCategory
//        }.toMutableList()

    //새로 고침 동작
    var isRefreshing by mutableStateOf(false)
        private set
//    fun refreshStudyList() {
//        // 강제로 약간의 지연을 줘야 애니메이션이 보임
//        viewModelScope.launch {
//            isRefreshing = true
//
//            val newList = _studyList.map {
//                it.copy()
//            }
//            _studyList.clear()
//            _studyList.addAll(newList)
//
//            delay(500) // 애니메이션만 보여주고 아무것도 안 함
//            isRefreshing = false
//        }
//    }

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


}