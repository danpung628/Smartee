package com.example.smartee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartee.ui.study.studyList.StudyDetailScreen
import com.example.smartee.ui.study.studyList.search.StudySearchScreen
import com.example.smartee.ui.attendance.AttendanceScreen
import com.example.smartee.ui.login.LoginScreen
import com.example.smartee.ui.profile.ProfileScreen
import com.example.smartee.ui.register.RegisterScreen
import com.example.smartee.ui.study.StudyCreateScreen
import com.example.smartee.ui.study.StudyListScreen


@Composable
fun SmarteeNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.StudyCreate.route) {
            StudyCreateScreen(navController)
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
                keyword = it.arguments!!.getString("keyword")!!,
                onStudyDetailNavigate = {
                    navController.navigate(Screen.Detail.route + "?studyID=$it")
                },
                onSearchNavigate = {
                    navController.navigate(Screen.Search.route)
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
                studyId = it.arguments!!.getString("ID")!!
            )
        }

        composable(Screen.Attendance.route) {
            AttendanceScreen(navController)
        }
    }
}
