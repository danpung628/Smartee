package com.example.smartee.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartee.navigation.Screen
import com.example.smartee.navigation.SmarteeNavGraph
import com.example.smartee.ui.theme.SmarteeTheme
import com.example.smartee.viewmodel.AuthViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(Screen.StudyList.route, Icons.Default.Home, "홈")
    object MyStudy : BottomNavItem("my_study", Icons.Default.List, "내 모임")
    object Profile : BottomNavItem(Screen.Profile.route, Icons.Default.AccountCircle, "프로필")
}

@Composable
fun SmarteeApp() {
    val authViewModel = viewModel<AuthViewModel>()
    val navController = rememberNavController()
    val viewModelStoreOwner = rememberViewModelStoreOwner()

    SmarteeTheme {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Scaffold(
            bottomBar = {
                val bottomBarItems = listOf(BottomNavItem.Home, BottomNavItem.MyStudy, BottomNavItem.Profile)
                val showBottomBar = bottomBarItems.any { it.route == currentDestination?.route }

                if (showBottomBar) {
                    NavigationBar {
                        bottomBarItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentDestination?.route == Screen.StudyList.route) {
                    FloatingActionButton(onClick = { navController.navigate(Screen.StudyCreate.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "스터디 생성")
                    }
                }
            }
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalNavGraphViewModelStoreOwner provides viewModelStoreOwner,
                LocalAuthViewModel provides authViewModel
            ) {
                SmarteeNavGraph(navController, Modifier.padding(innerPadding))

                // 알림 기능
                MeetingApplicationListener()
            }
        }
    }
}

@Composable
fun rememberViewModelStoreOwner(): ViewModelStoreOwner {
    val context = LocalContext.current
    return remember(context) { context as ViewModelStoreOwner }
}

val LocalNavGraphViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> { error("Undefined") }
val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> { error("AuthViewModel not provided") }