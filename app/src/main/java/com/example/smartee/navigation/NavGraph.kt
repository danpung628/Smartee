package com.example.smartee.navigation

import HostScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.attendance.AttendanceScreen
import com.example.smartee.ui.attendance.ParticipantScreen
import com.example.smartee.ui.login.LoginScreen
import com.example.smartee.ui.Map.NaverMapScreen
import com.example.smartee.ui.profile.ProfileScreen
import com.example.smartee.ui.screen.MyStudyScreen
import com.example.smartee.ui.screen.StudyCreationScreen
import com.example.smartee.ui.signup.FillProfileScreen
import com.example.smartee.ui.signup.SignUpScreen
import com.example.smartee.ui.study.editstudy.ui.StudyEditScreen
import com.example.smartee.ui.study.studyList.main.StudyListScreen
import com.example.smartee.ui.study.studyList.search.StudySearchScreen
import com.example.smartee.ui.study.studyList.studydetail.StudyDetailScreen


@Composable
fun SmarteeNavGraph(navController: NavHostController) {
    val authViewModel = LocalAuthViewModel.current
    val isLoggedIn by authViewModel.currentUser.collectAsState(initial = null)

    // 로그인 상태에 따라 시작 화면 결정
    val startDestination = if (isLoggedIn != null) {
        Screen.StudyList.route
    } else {
        Screen.SignUp.route
    }

    //출석코드
    val randomCode = remember { mutableStateOf((100..999).random()) }
    NavHost(navController, startDestination = Screen.SignUp.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.FillProfile.route) {
            FillProfileScreen(navController)
        }
        composable(Screen.StudyCreate.route) {
            StudyCreationScreen(navController)
        }

        //스터디 목록 관련
        //스터디 목록
        composable(
            route = Screen.StudyList.route + "?keyword={keyword}",
            arguments = listOf(
                navArgument("keyword") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            StudyListScreen(
//                keyword = it.arguments!!.getString("keyword")!!,
                onHomeNavigate = {
                    navController.navigate(Screen.StudyList.route)
                },
                onStudyDetailNavigate = {
                    navController.navigate(Screen.Detail.route + "?studyID=$it")
                },
                onSearchNavigate = {
                    navController.navigate(Screen.Search.route)
                },
                onStudyCreateNavigate = {
                    navController.navigate(Screen.StudyCreate.route)
                },
                onProfileNavigate = {
                    navController.navigate(Screen.Profile.route)
                },
                onMyStudyNavigate = {
                    navController.navigate("my_study")
                }
            )
        }
        //검색창
        composable(route = Screen.Search.route) {
            StudySearchScreen { keyword ->
                navController.navigate(Screen.StudyList.route + "?keyword=$keyword")
            }
        }
//스터디 세부사항
        composable(
            route = Screen.Detail.route + "?studyID={ID}",
            arguments = listOf(
                navArgument("ID") {
                    type = NavType.StringType
                }
            )
        ) {
            StudyDetailScreen(
                studyId = it.arguments!!.getString("ID")!!,
                onJoinStudy = { studyId ->
                    // 스터디 참가 로직
                    navController.navigate(Screen.Attendance.route)
                },
                onReportStudy = { studyId ->
                    // 스터디 신고 로직
                    // 필요하면 신고 화면으로 이동
                }
            )
        }
        //스터디 편집
        composable(
            route = Screen.StudyEdit.route + "?studyID={ID}",
            arguments = listOf(
                navArgument("ID") {
                    type = NavType.StringType
                }
            )
        ) {
            StudyEditScreen(
                studyId = it.arguments!!.getString("ID")!!
            )
        }

        composable(Screen.Attendance.route) {
            AttendanceScreen(navController)
        }
        //출석
        composable(Screen.Host.route) {
            HostScreen(navController, randomCode.value) { newCode ->
                randomCode.value = newCode
            }
        }
        composable(Screen.Participant.route) {
            ParticipantScreen(navController, randomCode.value)
        }
        //내 스터디
        composable("my_study") {
            MyStudyScreen()
        }


        composable("map") {
            NaverMapScreen()
        }

    }
}
