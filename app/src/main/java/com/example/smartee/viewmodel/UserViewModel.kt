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
                if (document != null && document.exists()) {
                    // [수정] toObject() 호출 시 발생할 수 있는 예외를 잡습니다.
                    try {
                        _userData.value = document.toObject(UserData::class.java)
                        android.util.Log.d("UserViewModel", "✅ 프로필 로딩 성공: ${_userData.value?.nickname}")
                    } catch (e: Exception) {
                        // 객체 변환 실패 시 로그를 남깁니다.
                        android.util.Log.e("UserViewModel", "❌ UserData 객체 변환 실패", e)
                    }
                } else {
                    android.util.Log.w("UserViewModel", "⚠️ 사용자 프로필 문서가 존재하지 않음")
                }
            }
            // [추가] Firestore에서 데이터를 가져오는 것 자체를 실패했을 때 로그를 남깁니다.
            .addOnFailureListener { e ->
                android.util.Log.e("UserViewModel", "❌ Firestore 문서 가져오기 실패", e)
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