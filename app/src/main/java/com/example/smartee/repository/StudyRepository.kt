package com.example.smartee.repository

import com.example.smartee.model.JoinRequest
import com.example.smartee.model.StudyData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studiesCollection = firestore.collection("studies")
    private val joinRequestsCollection = firestore.collection("joinRequests")

    suspend fun getAllStudies(): List<StudyData> {
        return try {
            val snapshot = studiesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(StudyData::class.java)?.copy(studyId = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStudyById(studyId: String): StudyData? {
        return try {
            val snapshot = studiesCollection.document(studyId).get().await()
            snapshot.toObject(StudyData::class.java)?.copy(studyId = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getStudiesByIds(studyIds: List<String>): List<StudyData> {
        if (studyIds.isEmpty()) {
            return emptyList()
        }
        return try {
            val snapshot = studiesCollection.whereIn(FieldPath.documentId(), studyIds).get().await()
            snapshot.documents.mapNotNull { it.toObject(StudyData::class.java)?.copy(studyId = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun checkIfRequestExists(userId: String, studyId: String): Boolean {
        return try {
            val snapshot = joinRequestsCollection
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("studyId", studyId)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    fun createJoinRequest(request: JoinRequest): Task<Void> {
        return joinRequestsCollection.document().set(request)
    }
}