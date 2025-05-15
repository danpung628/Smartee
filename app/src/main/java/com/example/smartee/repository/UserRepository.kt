package com.example.smartee.repository

import com.example.smartee.model.UserProfile
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

    fun saveUserProfile(userProfile: UserProfile): Task<Void> {
        return usersCollection.document(userProfile.uid).set(userProfile)
    }
}