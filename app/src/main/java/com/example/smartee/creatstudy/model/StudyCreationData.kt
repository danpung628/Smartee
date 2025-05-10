package com.example.smartee.model

import java.time.LocalDate

data class StudyCreationData(
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val maxParticipants: Int,
    val isOffline: Boolean,
    val minInk: Int,
    val isRegular: Boolean,
    val category: List<String>,
    val penCount: Int,
    val punishment: String?
)