package com.example.smartee.ui.study.editstudy.util

import com.example.smartee.model.StudyData
import java.time.LocalDate

fun validateStudy(data: StudyData): List<String> {
    val errors = mutableListOf<String>()

    if (data.title.isBlank()) errors.add("스터디 이름을 입력해주세요.")

    // String 타입의 날짜를 LocalDate로 변환 시도
    val startDate = try {
        if (data.startDate.isNotBlank()) LocalDate.parse(data.startDate) else null
    } catch (e: Exception) { null }

    val endDate = try {
        if (data.endDate.isNotBlank()) LocalDate.parse(data.endDate) else null
    } catch (e: Exception) { null }

    if (startDate == null) errors.add("시작일을 선택해주세요.")
    if (endDate == null) errors.add("종료일을 선택해주세요.")

    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
        errors.add("종료일은 시작일 이후여야 합니다.")
    }

    if (data.maxMemberCount <= 0) errors.add("최대 인원수는 1 이상이어야 합니다.")
    if (data.minInkLevel < 0) errors.add("최소 잉크는 0 이상이어야 합니다.")
    if (data.penCount < 0) errors.add("만년필 수는 0 이상이어야 합니다.")

    // 카테고리 체크 (쉼표로 구분된 문자열)
    if (data.category.isBlank()) errors.add("카테고리를 하나 이상 선택해주세요.")

    return errors
}