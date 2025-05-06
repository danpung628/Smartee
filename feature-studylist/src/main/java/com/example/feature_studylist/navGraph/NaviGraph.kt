package com.example.feature_studylist.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feature_studylist.model.Routes
import com.example.feature_studylist.uicomponents.screen.StudyDetailScreen
import com.example.feature_studylist.uicomponents.screen.StudySearchScreen
import com.example.smartee.ui.study.StudyListScreen

@Composable
fun NaviGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Routes.List.route) {
        //스터디 목록
        composable(
            route = Routes.List.route + "?keyword={keyword}",
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
                    navController.navigate(Routes.Detail.route + "?studyID=$it")
                },
                onSearchNavigate = {
                    navController.navigate(Routes.Search.route)
                }
            )
        }
        //검색창
        composable(route = Routes.Search.route) {
            StudySearchScreen { keyword ->
                navController.navigate(Routes.List.route + "?keyword=$keyword")
            }
        }
        //스터디 세부사항
        composable(
            route = Routes.Detail.route + "?studyID={ID}",
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
    }
}