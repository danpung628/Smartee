package com.example.smartee.repository

import com.example.smartee.model.StudyData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studiesCollection = firestore.collection("studies")

    // ✅ 전체 스터디 불러오기
    suspend fun getAllStudies(): List<StudyData> {
        return try {
            val snapshot = studiesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(StudyData::class.java)?.copy(studyId  = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
