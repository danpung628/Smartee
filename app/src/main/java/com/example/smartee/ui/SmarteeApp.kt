package com.example.smartee.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smartee.navigation.SmarteeNavGraph
import com.example.smartee.ui.theme.SmarteeTheme
import com.example.smartee.viewmodel.AuthViewModel

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

val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> {
    error("AuthViewModel not provided")
}

@Composable
fun SmarteeApp() {
    val authViewModel = viewModel<AuthViewModel>()
    val navController = rememberNavController()
    val viewModelStoreOwner = rememberViewModelStoreOwner()

    SmarteeTheme {
        Surface {
            CompositionLocalProvider(
                LocalNavGraphViewModelStoreOwner provides viewModelStoreOwner,
                LocalAuthViewModel provides authViewModel
            ) {
                SmarteeNavGraph(navController)
            }
        }
    }
}