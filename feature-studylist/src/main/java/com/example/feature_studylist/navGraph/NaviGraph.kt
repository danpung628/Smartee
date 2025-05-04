package com.example.feature_studylist.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feature_studylist.model.Routes
import com.example.feature_studylist.uicomponents.StudySearchScreen
import com.example.smartee.ui.study.StudyListScreen

@Composable
fun NaviGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Routes.List.route) {
        composable(
            route = Routes.List.route + "?keyword={keyword}",
            arguments = listOf(
                navArgument("keyword") {
                    type = NavType.StringType
                }
            )
        ) {
            StudyListScreen {
                navController.navigate(Routes.Search.route)
            }
        }
        composable(route = Routes.Search.route) {
            StudySearchScreen { keyword ->
                navController.navigate(Routes.List.route + keyword)
            }
        }
    }
}