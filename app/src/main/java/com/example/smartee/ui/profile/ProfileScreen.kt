// smartee/ui/profile/ProfileScreen.kt

package com.example.smartee.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
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
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "로그아웃")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (currentUser != null) {
            ProfileContent(
                modifier = Modifier.padding(paddingValues),
                currentUser = currentUser,
                userData = userData
            )
        } else {
            NotLoggedInContent(
                modifier = Modifier.padding(paddingValues),
                onLoginClick = { navController.navigate(Screen.SignUp.route) }
            )
        }
    }
}