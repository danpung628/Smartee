package com.example.smartee.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartee.model.UserData
import com.example.smartee.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class UserViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    init {
        // 🔥 이거 추가: ViewModel 생성될 때 유저 프로필 로드
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser ?: return

        userRepository.getUserProfile(currentUser.uid)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _userData.value = document.toObject(UserData::class.java)
                } else {
                    val newProfile = UserData(
                        uid = currentUser.uid,
                        name = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        interests = listOf(),
                        ink = 50,
                        pen = 2
                    )
                    _userData.value = newProfile
                    userRepository.saveUserProfile(newProfile)
                }
            }
    }
}

class UserViewModelFactory(application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val userRepository = UserRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance())
            val auth = FirebaseAuth.getInstance()
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}