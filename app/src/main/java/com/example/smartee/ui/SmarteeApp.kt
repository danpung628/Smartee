package com.example.smartee.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import com.example.smartee.navigation.SmarteeNavGraph
import com.example.smartee.ui.theme.SmarteeTheme

@Composable
fun rememberViewModelStoreOwner(): ViewModelStoreOwner {
    val context = LocalContext.current
    return remember(context) {
        context as ViewModelStoreOwner
    }
}

val LocalNavGraphViewModelStoreOwner =
    staticCompositionLocalOf<ViewModelStoreOwner> {
        error("Undefined")
    }

@Composable
fun SmarteeApp() {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val navController = rememberNavController()

    SmarteeTheme {
        Surface {
            CompositionLocalProvider(LocalNavGraphViewModelStoreOwner provides viewModelStoreOwner) {
                SmarteeNavGraph(navController)
            }
        }
    }
}