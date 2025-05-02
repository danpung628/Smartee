package com.example.smartee.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.smartee.navigation.SmarteeNavGraph
import com.example.smartee.ui.theme.SmarteeTheme

@Composable
fun SmarteeApp() {
    val navController = rememberNavController()

    SmarteeTheme {
        Surface {
            SmarteeNavGraph(navController)
        }
    }
}