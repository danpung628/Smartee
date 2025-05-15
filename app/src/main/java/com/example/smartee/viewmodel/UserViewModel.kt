package com.example.smartee.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartee.model.UserProfile
import com.example.smartee.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class UserViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser ?: return

        userRepository.getUserProfile(currentUser.uid)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _userProfile.value = document.toObject(UserProfile::class.java)
                } else {
                    // 신규 사용자인 경우 기본 프로필 생성
                    val newProfile = UserProfile(
                        uid = currentUser.uid,
                        displayName = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        interests = listOf(),
                        inkLevel = 50
                    )
                    _userProfile.value = newProfile
                    // Firestore에 저장
                    userRepository.saveUserProfile(newProfile)
                }
            }
    }

    fun updateUserInterests(interests: List<String>) {
        val currentUser = auth.currentUser ?: return
        userRepository.updateUserInterests(currentUser.uid, interests)
            .addOnSuccessListener {
                // 현재 프로필 업데이트
                _userProfile.value = _userProfile.value?.copy(interests = interests)
            }
    }
}