package com.example.feature_studylist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature_studylist.model.StudyData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel : ViewModel() {
    private val _studyList = mutableStateListOf(
        StudyData(
            studyId = "0",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "1",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2,
            thumbnailModel = "https://encrypted-tbn3.gstatic.com/licensed-image?q=tbn:ANd9GcQ7VhtX7lpCH-hf4qOD-SZm3h1Oa2PiPkxzupBr4CtJZwNnB3jgSn2tPg5vZkwSJv46TloTo9nEUqatww8"
        ),
        StudyData(
            studyId = "2",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE,
            thumbnailModel = "https://m.media-amazon.com/images/I/613sHer5J-L._AC_UF894,1000_QL80_.jpg"

        ),
        StudyData(
            studyId = "3",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11,
            thumbnailModel = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSMzFSnBu0CbZFsE9jifLZ5pbJlMxIxuTei_w&s"
        ),
        StudyData(
            studyId = "4",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "5",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "6",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "7",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "8",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "9",
            title = "영어 스터디",
            address = "군자동",
            maxMemberCount = 6,
            commentCount = 3,
            likeCount = 11,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "10",
            title = "C++ 스터디 모집합니다.",
            address = "광진구 구의제3동",
            maxMemberCount = 6,
            likeCount = 2,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
        StudyData(
            studyId = "11",
            title = "매주 금요일 밤에 풋살하실 분~",
            address = "동대문구 휘경동",
            maxMemberCount = Int.MAX_VALUE,
            thumbnailModel = "https://i.namu.wiki/i/16b-ElplD3LJ2DxvVcmW89cqxkOh0rqykfKgdIep8yy9eOriyEIDARUKvBeaXk6Lo_qduMkx3_IR4cfrZaqNtJjPY5cCpDywbdBEISz0jckcmNP-vdrwLAPHKzyo4pIvTVMpKcVXAnKGEDhuV0sGtWEsjXUI4R08kX4GPhiPj1w.webp"
        ),
    )
    val studyList: MutableList<StudyData>
        get() = _studyList

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