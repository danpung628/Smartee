package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.UserData
import com.example.smartee.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.catch

class UserViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // [수정] 사용자 ID가 변경될 때마다 새로운 Flow를 구독하고 LiveData로 변환
    val userData: LiveData<UserData?> = auth.currentUser?.uid?.let { userId ->
        userRepository.getUserProfileFlow(userId)
            .catch { e -> Log.e("UserViewModel", "Flow 수집 오류", e) }
            .asLiveData(viewModelScope.coroutineContext)
    } ?: MutableLiveData(null) // 사용자가 로그아웃 상태일 경우 null을 가진 LiveData 반환

}

// UserViewModelFactory는 기존과 동일
class UserViewModelFactory(application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            val userRepository = UserRepository(firestore)
            val auth = FirebaseAuth.getInstance()
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository, auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}