package com.example.smartee.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.Meeting
import com.example.smartee.repository.StudyRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MeetingCreationViewModel : ViewModel() {
    private val studyRepository = StudyRepository()

    var title by mutableStateOf("")
    var date by mutableStateOf<LocalDate?>(null)
    var startTime by mutableStateOf<LocalTime?>(null)
    var endTime by mutableStateOf<LocalTime?>(null)
    var isOffline by mutableStateOf(true)
    var location by mutableStateOf("")
    var description by mutableStateOf("")
    var maxParticipants by mutableStateOf("")

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private fun validate(): String? {
        if (title.isBlank()) return "모임 제목을 입력해주세요."
        if (date == null) return "날짜를 선택해주세요."
        if (startTime == null) return "시작 시간을 선택해주세요."
        if (endTime == null) return "종료 시간을 선택해주세요."
        if (startTime!!.isAfter(endTime!!)) return "시작 시간은 종료 시간보다 빨라야 합니다."
        if (maxParticipants.toIntOrNull() == null) return "참여 인원은 숫자만 입력 가능합니다."
        return null
    }

    fun createMeeting(parentStudyId: String) {
        Log.d("ID_TRACE", "ViewModel에서 저장하려는 parentStudyId: $parentStudyId")

        val validationError = validate()
        if (validationError != null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowSnackbar(validationError))
            }
            return
        }

        viewModelScope.launch {
            val meeting = Meeting(
                parentStudyId = parentStudyId,
                title = title,
                date = date!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                time = "${startTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}~${endTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                isOffline = isOffline,
                location = if (isOffline) location else "온라인",
                description = description,
                maxParticipants = maxParticipants.toInt()
            )

            studyRepository.createMeeting(meeting)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.ShowSnackbar("모임 생성 완료!"))
                        _uiEvent.emit(UiEvent.NavigateBack)
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.ShowSnackbar("오류가 발생했습니다: ${it.message}"))
                    }
                }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}