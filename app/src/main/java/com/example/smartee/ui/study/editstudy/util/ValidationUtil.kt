package com.example.smateeeeeeeeeeeeeeeeeeeeeeeee.editstudy.util



import study_edit.viewmodel.StudyCreationData

fun validateStudy(data: StudyCreationData): List<String> {
    val errors = mutableListOf<String>()

    if (data.name.isBlank()) errors.add("스터디 이름을 입력해주세요.")
    if (data.startDate == null) errors.add("시작일을 선택해주세요.")
    if (data.endDate == null) errors.add("종료일을 선택해주세요.")
    if (data.startDate != null && data.endDate != null && data.endDate.isBefore(data.startDate)) {
        errors.add("종료일은 시작일 이후여야 합니다.")
    }
    if (data.maxParticipants <= 0) errors.add("최대 인원수는 1 이상이어야 합니다.")
    if (data.minInk < 0) errors.add("최소 잉크는 0 이상이어야 합니다.")
    if (data.penCount < 0) errors.add("만년필 수는 0 이상이어야 합니다.")
    if (data.selectedCategories.isEmpty()) errors.add("카테고리를 하나 이상 선택해주세요.")

    return errors
}
