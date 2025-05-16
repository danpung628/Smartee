package com.example.smartee.ui.signup

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartee.R
import com.example.smartee.navigation.Screen
import com.example.smartee.ui.common.LoadingOverlay
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = remember { FirebaseAuth.getInstance() }
    val oneTapClient = remember { Identity.getSignInClient(context) }
    var isLoading by remember { mutableStateOf(false) }

    // 로그인된 유저가 있으면 바로 홈으로
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // 기존 유저 → 홈 또는 로그인 화면
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    } else {
                        // 신규 유저이지만 앱 재실행한 경우 → 프로필 입력부터 다시
                        navController.navigate(Screen.FillProfile.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SignUpScreen", "자동 로그인 시 Firestore 조회 실패: ${e.message}")
                }
        }
    }

    val signInRequest = remember {
        BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false) // 모든 계정 허용
                    .build()
            )
            .setAutoSelectEnabled(true) // 최근 계정 자동 로그인
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
                    .addOnSuccessListener {
                        val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                        val db = FirebaseFirestore.getInstance()

                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // 기존 유저 → 홈 화면으로 이동
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.SignUp.route) { inclusive = true }
                                    }
                                } else {
                                    // 신규 유저 → 프로필 입력 화면
                                    navController.navigate(Screen.FillProfile.route) {
                                        popUpTo(Screen.SignUp.route) { inclusive = true }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("SignUpScreen", "Firestore 조회 실패: ${e.message}")
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

    Box {
        Column {
            Button(onClick = {
                isLoading = true
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        val request = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        launcher.launch(request)
                    }
                    .addOnFailureListener {
                        Log.e("SignUpScreen", "One Tap Sign-In 실패: ${it.message}")
                        isLoading = false
                    }
            }) {
                Text("Google로 시작하기")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                auth.signOut()
                navController.navigate(Screen.Login.route)
            }) {
                Text("개발자 모드 (로그아웃)")
            }
        }

        // 👇 로딩 중일 때 오버레이 표시
        if (isLoading) {
            LoadingOverlay()
        }
    }
}
