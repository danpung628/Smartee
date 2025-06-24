package com.example.smartee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.Meeting
import com.example.smartee.repository.StudyRepository
import com.example.smartee.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

        val currentUserId = UserRepository.getCurrentUserId()
        if (currentUserId == null) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("사용자 정보를 가져올 수 없습니다. 다시 로그인해주세요.")) }
            return
        }

        val meetingMap = mutableMapOf<String, Any>(
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

        viewModelScope.launch {
            try {
                if (currentMeetingId == null) {
                    // [추가] 생성 모드일 때만, 생성자를 첫 참여자로 추가합니다.
                    meetingMap["confirmedParticipants"] = listOf(currentUserId)

                    studyRepository.createMeeting(meetingMap, parentStudyId).await()
                    _uiEvent.emit(UiEvent.ShowSnackbar("모임 생성 완료!"))
                } else {
                    // 수정 모드
                    studyRepository.updateMeeting(currentMeetingId, meetingMap).await()
                    _uiEvent.emit(UiEvent.ShowSnackbar("모임 수정 완료!"))
                }
                _uiEvent.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("오류가 발생했습니다: ${e.message}"))
            }
        }
    }

    // [수정] 모임 삭제 함수
    fun deleteMeeting() {
        val currentMeetingId = meetingId
        if (currentMeetingId == null) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("삭제할 수 없는 모임입니다.")) }
            return
        }

        viewModelScope.launch {
            try {
                // 1. ID를 사용해 Repository에서 Meeting 객체를 가져옴
                val meetingToDelete = studyRepository.getMeetingById(currentMeetingId)

                if (meetingToDelete != null) {
                    // 2. 가져온 Meeting 객체를 파라미터로 전달하여 삭제 실행
                    studyRepository.deleteMeeting(meetingToDelete).await() // .await()으로 작업 완료 대기
                    _uiEvent.emit(UiEvent.ShowSnackbar("모임이 삭제되었습니다."))
                    _uiEvent.emit(UiEvent.NavigateBack)
                } else {
                    _uiEvent.emit(UiEvent.ShowSnackbar("삭제할 모임 정보를 찾을 수 없습니다."))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("삭제 중 오류가 발생했습니다: ${e.message}"))
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