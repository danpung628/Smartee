package com.example.smartee.viewmodel

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

class MeetingEditViewModel : ViewModel() {
    private val studyRepository = StudyRepository()

    // UI State
    var meetingId by mutableStateOf<String?>(null)
        private set
    var title by mutableStateOf("")
    var date by mutableStateOf<LocalDate?>(null)
    var startTime by mutableStateOf<LocalTime?>(null)
    var endTime by mutableStateOf<LocalTime?>(null)
    var isOffline by mutableStateOf(true)
    var location by mutableStateOf("")
    var description by mutableStateOf("")
    var maxParticipants by mutableStateOf("")

    // UI Event
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun loadMeeting(id: String) {
        meetingId = id
        viewModelScope.launch {
            studyRepository.getMeetingById(id)?.let { meeting ->
                title = meeting.title
                date = LocalDate.parse(meeting.date)
                val times = meeting.time.split("~")
                if (times.size == 2) {
                    startTime = LocalTime.parse(times[0])
                    endTime = LocalTime.parse(times[1])
                }
                isOffline = meeting.isOffline
                location = meeting.location
                description = meeting.description
                maxParticipants = meeting.maxParticipants.toString()
            }
        }
    }

    fun saveMeeting(parentStudyId: String) {
        val validationError = validate()
        if (validationError != null) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar(validationError)) }
            return
        }

        val meetingMap = mapOf(
            "parentStudyId" to parentStudyId,
            "title" to title,
            "date" to date!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
            "time" to "${startTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}~${endTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))}",
            "isOffline" to isOffline,
            "location" to if (isOffline) location else "온라인",
            "description" to description,
            "maxParticipants" to (maxParticipants.toIntOrNull() ?: 0)
        )

        val currentMeetingId = meetingId
        val task = if (currentMeetingId == null) {
            // 생성 모드
            studyRepository.createMeeting(meetingMap, parentStudyId)
        } else {
            // 수정 모드
            studyRepository.updateMeeting(currentMeetingId, meetingMap)
        }

        task.addOnSuccessListener {
            viewModelScope.launch {
                val message = if (currentMeetingId == null) "모임 생성 완료!" else "모임 수정 완료!"
                _uiEvent.emit(UiEvent.ShowSnackbar(message))
                _uiEvent.emit(UiEvent.NavigateBack)
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowSnackbar("오류가 발생했습니다: ${it.message}"))
            }
        }
    }

    private fun validate(): String? {
        if (title.isBlank()) return "모임 제목을 입력해주세요."
        if (date == null) return "날짜를 선택해주세요."
        if (startTime == null) return "시작 시간을 선택해주세요."
        if (endTime == null) return "종료 시간을 선택해주세요."
        if (startTime!!.isAfter(endTime!!)) return "시작 시간은 종료 시간보다 빨라야 합니다."
        if (maxParticipants.toIntOrNull() == null) return "참여 인원은 숫자만 입력 가능합니다."
        return null
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}