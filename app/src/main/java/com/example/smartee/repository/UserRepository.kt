package com.example.smartee.repository

import com.example.smartee.model.UserData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    fun getUserProfile(userId: String): Task<DocumentSnapshot> {
        return usersCollection.document(userId).get()
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
}