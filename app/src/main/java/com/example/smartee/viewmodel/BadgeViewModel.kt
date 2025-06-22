// smartee/viewmodel/BadgeViewModel.kt
package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.UserData
import com.example.smartee.ui.badge.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BadgeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges = _badges.asStateFlow()

    fun loadBadges() {
        viewModelScope.launch {
            try {
                val allBadgesSnapshot = firestore.collection("badges").get().await()
                val allBadgesMap = allBadgesSnapshot.documents.map { doc ->
                    doc.id to Badge(
                        badgeId = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        unlockedImageUrl = doc.getString("unlockedImageUrl") ?: "", // 수정
                        lockedImageUrl = doc.getString("lockedImageUrl") ?: ""      // 수정
                    )
                }.toMap()

                val userId = auth.currentUser?.uid ?: return@launch
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userData = userDoc.toObject(UserData::class.java)
                val earnedBadgeIds = userData?.earnedBadgeIds?.toSet() ?: emptySet()

                val finalBadgeList = allBadgesMap.values.map { badge ->
                    badge.copy(isUnlocked = earnedBadgeIds.contains(badge.badgeId))
                }.sortedByDescending { it.isUnlocked } // 획득한 뱃지를 위로
                _badges.value = finalBadgeList

            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}