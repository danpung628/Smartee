//package com.example.smartee.ui.splash
//
//import android.app.Application
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.smartee.navigation.Screen
//import com.example.smartee.viewmodel.AuthViewModel
//import com.example.smartee.viewmodel.UserViewModel
//import com.example.smartee.viewmodel.UserViewModelFactory
//import kotlinx.coroutines.delay
//
//@Composable
//fun SplashScreen(navController: NavController) {
//    // ViewModel 인스턴스들을 가져옵니다.
//    val authViewModel: AuthViewModel = viewModel()
//    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current.applicationContext as Application))
//
//    // 화면 중앙에 로딩 인디케이터를 표시합니다.
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        CircularProgressIndicator()
//    }
//
//    // ViewModel에서 LiveData로 제공되는 데이터들을 관찰합니다.
//    val currentUser by authViewModel.currentUser.collectAsState()
//    val userData by userViewModel.userData.observeAsState()
//
//    // 인증 상태 확인이 완료되었는지를 추적하기 위한 상태
//    val authStateDetermined = remember { mutableStateOf(false) }
//
//    // 앱 시작 시 최소 1.5초간 스플래시를 보여주고, 인증 상태가 확인되었음을 표시합니다.
//    // 이 delay는 로고를 보여주거나 초기화 작업을 위한 시간을 확보하는 데 사용될 수 있습니다.
//    LaunchedEffect(Unit) {
//        delay(1500)
//        authStateDetermined.value = true
//    }
//
//    // 인증 상태가 결정되었다면, 상태에 따라 화면을 전환합니다.
//    if (authStateDetermined.value) {
//        LaunchedEffect(currentUser, userData) {
//            // currentUser가 null이 아니면서, userData가 아직 null일 때는 로딩중이므로 아무것도 하지 않고 기다립니다.
//            // CircularProgressIndicator가 계속 보이게 됩니다.
//            if (currentUser != null && userData == null) {
//                return@LaunchedEffect // 로딩 중...
//            }
//
//            // 최종 상태가 결정되면 화면을 한 번만 이동시킵니다.
//            if (currentUser == null) {
//                // Case 1: 로그아웃 상태 -> 회원가입 화면으로
//                navController.navigate(Screen.SignUp.route) {
//                    popUpTo("splash_screen") { inclusive = true }
//                }
//            } else { // currentUser is not null and userData is not null
//                if (userData?.nickname.isNullOrBlank()) {
//                    // Case 2: 로그인 O, 프로필 작성 X -> 프로필 작성 화면으로
//                    navController.navigate(Screen.FillProfile.route) {
//                        popUpTo("splash_screen") { inclusive = true }
//                    }
//                } else {
//                    // Case 3: 로그인 O, 프로필 작성 O -> 메인 화면으로
//                    navController.navigate(Screen.StudyList.route) {
//                        popUpTo("splash_screen") { inclusive = true }
//                    }
//                }
//            }
//        }
//    }
//}