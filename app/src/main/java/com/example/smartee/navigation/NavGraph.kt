package com.example.smartee.navigation

import HostScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.request.RequestListScreen
import com.example.smartee.ui.attendance.AttendanceScreen
import com.example.smartee.ui.attendance.ParticipantScreen
import com.example.smartee.ui.login.LoginScreen
import com.example.smartee.ui.Map.NaverMapScreen
import com.example.smartee.ui.MeetingCreationScreen
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
fun SmarteeNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val authViewModel = LocalAuthViewModel.current
    val isLoggedIn by authViewModel.currentUser.collectAsState(initial = null)

    val startDestination = if (isLoggedIn != null) {
        Screen.StudyList.route
    } else {
        Screen.SignUp.route
    }

    val randomCode = remember { mutableStateOf((100..999).random()) }
    NavHost(
        navController = navController,
        startDestination = Screen.SignUp.route,
        modifier = modifier
    ) {
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
        composable(
            route = Screen.StudyList.route
        ) {
            StudyListScreen(
                onStudyDetailNavigate = { studyId ->
                    navController.navigate(Screen.Detail.route + "?studyID=$studyId")
                },
                onSearchNavigate = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }
        composable(route = Screen.Search.route) {
            StudySearchScreen { keyword ->
                navController.navigate(Screen.StudyList.route + "?keyword=$keyword")
            }
        }
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
                navController = navController
            )
        }
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
        composable(Screen.Host.route) {
            HostScreen(navController, randomCode.value) { newCode ->
                randomCode.value = newCode
            }
        }
        composable(Screen.Participant.route) {
            ParticipantScreen(navController, randomCode.value)
        }
        composable("my_study") {
            MyStudyScreen(
                onStudyClick = { studyId ->
                    navController.navigate(Screen.Detail.route + "?studyID=$studyId")
                }
            )
        }
        composable("map") {
            NaverMapScreen()
        }
        composable(
            route = "request_list/{studyId}",
            arguments = listOf(navArgument("studyId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequestListScreen(studyId = backStackEntry.arguments?.getString("studyId") ?: "")
        }
        composable(
            route = "create_meeting/{parentStudyId}",
            arguments = listOf(navArgument("parentStudyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val receivedId = backStackEntry.arguments?.getString("parentStudyId") ?: ""
            // 전달받은 ID 값을 로그로 출력
            Log.d("ID_TRACE", "NavGraph에서 전달받은 ID: $receivedId")

            MeetingCreationScreen(
                parentStudyId = receivedId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}