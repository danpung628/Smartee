package com.example.smartee.ui.signup

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smartee.R
import com.example.smartee.navigation.Screen
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = remember { FirebaseAuth.getInstance() }

    val oneTapClient = remember { Identity.getSignInClient(context) }

    val signInRequest = remember {
        BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { authResult ->
                        val isNewUser = authResult.additionalUserInfo?.isNewUser == true
                        Log.d("SignUpScreen", "로그인 성공, 신규 유저 여부: $isNewUser")

                        if (isNewUser) {
                            navController.navigate(Screen.FillProfile.route)
                        } else {
                            navController.navigate(Screen.StudyList.route)
                        }
                    }
                    .addOnFailureListener {
                        Log.e("SignUpScreen", "Firebase 인증 실패: ${it.message}")
                    }
            } else {
                Log.e("SignUpScreen", "ID 토큰이 없음")
            }
        } catch (e: ApiException) {
            Log.e("SignUpScreen", "Google 로그인 실패", e)
        }
    }

    Button(onClick = {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val request = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(request)
            }
            .addOnFailureListener {
                Log.e("SignUpScreen", "One Tap Sign-In 실패: ${it.message}")
            }
    }) {
        Text("Google로 시작하기")
    }
}
