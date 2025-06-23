package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.StudyData
import com.example.smartee.model.factory.CategoryListFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class StudyViewModel(app: Application) : AndroidViewModel(app) {
    //새로 고침 동작
    var isRefreshing by mutableStateOf(false)
        private set

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
                    selectedCategory.any { cat -> it.category.contains(cat) }
        }.toMutableList()
    }

    //드롭다운에서 선택한 주소
    var selectedAddress by mutableStateOf("")
    //스터디 검색창에 현재 입력한 텍스트
    var typedText by mutableStateOf("")
    //실제로 검색할 스터디 키워드
    var searchKeyword by mutableStateOf("")
    //카테고리 체크
    var selectedCategory by mutableStateOf(CategoryListFactory.makeCategoryList().toList())

    // 초기화 시 Firebase에서 데이터 불러오기
    init {
        loadStudiesFromFirebase()
    }

    // 스터디 데이터 로드 후 추천 갱신을 위한 콜백
    var onStudiesLoaded: ((List<StudyData>) -> Unit)? = null
    // Firebase에서 스터디 목록 불러오기
    private fun loadStudiesFromFirebase() {
        viewModelScope.launch {
            try {
                isRefreshing = true  // 여기서 오류가 발생하면

                studyCollectionRef.get()
                    .addOnSuccessListener { documents ->
                        val studyList = mutableListOf<StudyData>()
                        Log.d(
                            "StudyViewModel",
                            "=== Firebase에서 로드된 전체 문서 수: ${documents.size()} ==="
                        )

                        for (document in documents) {
                            val study = document.toObject(StudyData::class.java)
                                .copy(studyId = document.id)
                            studyList.add(study)
                            Log.d(
                                "StudyViewModel",
                                "로드된 스터디: ${study.title}, 카테고리: ${study.category}"
                            )
                        }

                        _studyList.value = studyList
                        Log.d("StudyViewModel", "=== 최종 스터디 목록 크기: ${studyList.size} ===")

                        // 독서 스터디만 따로 확인
                        val readingStudies = studyList.filter { it.category.contains("독서") }
                        Log.d("StudyViewModel", "독서 스터디 개수: ${readingStudies.size}")
                        readingStudies.forEach {
                            Log.d("StudyViewModel", "독서 스터디: ${it.title}")
                        }

                        // 콜백을 통해 RecommendationViewModel에 알림
                        onStudiesLoaded?.invoke(studyList)

                        isRefreshing = false
                    }
                    .addOnFailureListener { e ->
                        // 오류 처리
                        Log.e("StudyViewModel", "Error loading studies", e)
                        isRefreshing = false
                    }
            } catch (e: Exception) {
                Log.e("StudyViewModel", "Fatal error in refreshing", e)
            }
        }
    }

    // 새로고침 시 Firebase에서 다시 불러오기
    fun refreshStudyList() {
        loadStudiesFromFirebase()
    }

    fun toggleCategory(category: String) {
        selectedCategory = if (category in selectedCategory) {
            selectedCategory - category
        } else {
            selectedCategory + category
        }
    }
}
