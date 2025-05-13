
package study_edit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class StudyEditViewModel : ViewModel() {
    var name by mutableStateOf("")
    var startDate by mutableStateOf<LocalDate?>(null)
    var endDate by mutableStateOf<LocalDate?>(null)
    var maxParticipants by mutableStateOf("")
    var isOffline by mutableStateOf(false)
    var minInk by mutableStateOf("")
    var isRegular by mutableStateOf(false)
    var selectedCategories = mutableStateListOf<String>()
    var penCount by mutableStateOf("")

    fun loadStudyData(data: StudyCreationData) {
        name = data.name
        startDate = data.startDate
        endDate = data.endDate
        maxParticipants = data.maxParticipants.toString()
        isOffline = data.isOffline
        minInk = data.minInk.toString()
        isRegular = data.isRegular
        selectedCategories.clear()
        selectedCategories.addAll(data.selectedCategories)
        penCount = data.penCount.toString()
    }
}

// Dummy data model to illustrate structure
data class StudyCreationData(
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val maxParticipants: Int,
    val isOffline: Boolean,
    val minInk: Int,
    val isRegular: Boolean,
    val selectedCategories: List<String>,
    val penCount: Int
)
