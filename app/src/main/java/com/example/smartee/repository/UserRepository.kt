package com.example.smartee.repository

import com.example.smartee.model.UserData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    fun getUserProfile(userId: String): Task<DocumentSnapshot> {
        return usersCollection.document(userId).get()
    }

    // [추가] 사용자의 프로필 변경을 실시간으로 감지하여 Flow로 반환하는 함수
    fun getUserProfileFlow(userId: String): Flow<UserData?> = callbackFlow {
        val listenerRegistration = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Flow를 에러와 함께 닫음
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(UserData::class.java)) // 성공적으로 데이터를 Flow로 보냄
                } else {
                    trySend(null) // 문서가 없는 경우 null을 보냄
                }
            }
        // Flow가 취소될 때 (ViewModel이 사라질 때 등) 리스너를 자동으로 제거
        awaitClose { listenerRegistration.remove() }
    }

    fun updateUserInterests(userId: String, interests: List<String>): Task<Void> {
        return usersCollection.document(userId).update("interests", interests)
    }

    fun updateUserInkLevel(userId: String, inkLevel: Int): Task<Void> {
        return usersCollection.document(userId).update("inkLevel", inkLevel)
    }

    fun saveUserProfile(userData: UserData): Task<Void> {
        return usersCollection.document(userData.uid).set(userData)
    }

    fun updatePenCount(userId: String, penCount: Int): Any {
        return usersCollection.document(userId).update("penCount", penCount)
    }

    companion object {
        fun getCurrentUserId(): String? {
            return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        }
    }
    fun addCreatedStudyId(userId: String, studyId: String): Task<Void> {
        // arrayUnion은 배열에 중복되지 않게 원소를 추가합니다.
        return usersCollection.document(userId).update("createdStudyIds", FieldValue.arrayUnion(studyId))
    }

    fun addJoinedStudyId(userId: String, studyId: String): Task<Void> {
        return usersCollection.document(userId).update("joinedStudyIds", FieldValue.arrayUnion(studyId))
    }

    fun updateUserProfile(userId: String, data: Map<String, Any>): Task<Void> {
        return usersCollection.document(userId).update(data)
    }
}