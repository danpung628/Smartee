package com.example.smartee.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.UserData
import com.example.smartee.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileEditViewModel : ViewModel() {
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())
    private val auth = FirebaseAuth.getInstance()

    // UI 상태 변수
    var nickname by mutableStateOf("")
    var selectedSido by mutableStateOf("")
    var selectedSigungu by mutableStateOf("")
    val selectedInterests = mutableStateListOf<String>()

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        isLoading = true
        viewModelScope.launch {
            try {
                val document = userRepository.getUserProfile(userId).await()
                val user = document.toObject(UserData::class.java)
                if (user != null) {
                    nickname = user.nickname
                    selectedInterests.clear()
                    selectedInterests.addAll(user.interests)
                    val regionParts = user.region.split(" ")
                    if (regionParts.size == 2) {
                        selectedSido = regionParts[0]
                        selectedSigungu = regionParts[1]
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileEditViewModel", "프로필 로드 실패", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun saveProfile(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        if (nickname.isBlank() || selectedSido.isBlank() || selectedSigungu.isBlank() || selectedInterests.isEmpty()) {
            onFailure("모든 항목을 입력해주세요.")
            return
        }
        isLoading = true

        val updatedData = mapOf(
            "nickname" to nickname,
            "region" to "$selectedSido $selectedSigungu",
            "interests" to selectedInterests.toList()
        )

        viewModelScope.launch {
            try {
                userRepository.updateUserProfile(userId, updatedData).await()
                onSuccess()
            } catch (e: Exception) {
                onFailure("저장 중 오류가 발생했습니다: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleInterest(interest: String) {
        if (selectedInterests.contains(interest)) {
            selectedInterests.remove(interest)
        } else {
            selectedInterests.add(interest)
        }
    }
}