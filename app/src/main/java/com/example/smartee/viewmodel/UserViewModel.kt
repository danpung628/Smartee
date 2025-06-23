package com.example.smartee.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.UserData
import com.example.smartee.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest

// AuthViewModel의 로그인 상태(currentUser)를 기반으로
// UserRepository의 프로필 정보(Flow)를 가져와 LiveData로 변환합니다.
class UserViewModel(
    private val userRepository: UserRepository,
    authViewModel: AuthViewModel // AuthViewModel을 직접 주입받습니다.
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val userData: LiveData<UserData?> = authViewModel.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                // 로그인 상태이면, 해당 유저의 프로필 Flow를 반환합니다.
                userRepository.getUserProfileFlow(user.uid)
            } else {
                // 로그아웃 상태이면, null을 가진 Flow를 즉시 반환합니다.
                kotlinx.coroutines.flow.flowOf(null)
            }
        }
        .catch { e ->
            // Flow 처리 중 에러가 발생하면 로그를 남깁니다.
            Log.e("UserViewModel", "Failed to collect user profile flow", e)
        }
        .asLiveData(viewModelScope.coroutineContext)
}

// UserViewModelFactory는 UserViewModel에 AuthViewModel을 주입해주는 역할을 합니다.
class UserViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            val userRepository = UserRepository(firestore)
            // AuthViewModel 인스턴스를 직접 생성하여 UserViewModel에 전달합니다.
            // 이 방식은 ViewModel들이 서로를 직접 참조하게 되므로,
            // 더 큰 규모의 앱에서는 Hilt와 같은 의존성 주입 라이브러리 사용을 고려해볼 수 있습니다.
            val authViewModel = AuthViewModel()
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}