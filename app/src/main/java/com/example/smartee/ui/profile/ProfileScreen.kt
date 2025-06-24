// smartee/ui/profile/ProfileScreen.kt

package com.example.smartee.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.navigation.Screen
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.viewmodel.UserViewModel
import com.example.smartee.viewmodel.UserViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val authViewModel = LocalAuthViewModel.current
    val currentUser by authViewModel.currentUser.collectAsState()

    val userViewModel: UserViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current,
        factory = UserViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val userData by userViewModel.userData.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 프로필") },
                actions = {
                    if (currentUser != null) {
                        // [수정] onClick에 네비게이션 로직 추가
                        IconButton(onClick = { navController.navigate(Screen.ProfileEdit.route) }) {
                            Icon(Icons.Default.Edit, contentDescription = "프로필 편집")
                        }
                        IconButton(onClick = {
                            authViewModel.signOut()
                            // navigate 부분을 삭제!
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "로그아웃")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // [수정] userData가 null이 아닐 경우에만 ProfileContent를 보여줍니다.
        // 이는 데이터가 로드 중이거나, 프로필이 없거나, 로그아웃 상태일 때를 모두 처리합니다.
        if (currentUser != null && userData != null) {
            ProfileContent(
                modifier = Modifier.padding(paddingValues),
                currentUser = currentUser,
                userData = userData,
                onAdminClick = { navController.navigate(Screen.AdminReport.route) }
            )
        } else if (currentUser != null && userData == null) {
            // [추가] 데이터 로딩 중을 표시하거나, 프로필이 없는 사용자를 위한 화면을 보여줄 수 있습니다.
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator() // 예: 로딩 인디케이터
            }
        } else {
            NotLoggedInContent(
                modifier = Modifier.padding(paddingValues),
                onLoginClick = { navController.navigate(Screen.SignUp.route) }
            )
        }
    }
}