package com.example.smartee.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartee.navigation.Screen
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.ink.InkScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val authViewModel = LocalAuthViewModel.current
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 프로필") },
                actions = {
                    if (currentUser != null) {
                        IconButton(onClick = { /* 프로필 편집 화면으로 이동 */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "프로필 편집")
                        }
                        IconButton(onClick = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.Logout, contentDescription = "로그아웃")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (currentUser != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 프로필 이미지
                AsyncImage(
                    model = currentUser?.photoUrl ?: "https://picsum.photos/300/300",
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 사용자 이름
                Text(
                    text = currentUser?.displayName ?: "사용자",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 이메일
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 잉크 섹션 (기존 InkScreen 컴포넌트 활용)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    InkScreen()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 참여 중인 스터디 섹션
                Text(
                    text = "참여 중인 스터디",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 여기에 참여 중인 스터디 목록을 표시하는 LazyColumn 추가
                // StudyListItem 컴포넌트를 재사용할 수 있음

                // 아직 참여 중인 스터디가 없는 경우 표시
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("아직 참여 중인 스터디가 없습니다")
                    }
                }
            }
        } else {
            // 로그인되지 않은 경우
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("로그인이 필요합니다")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate(Screen.SignUp.route) }) {
                    Text("로그인 하러 가기")
                }
            }
        }
    }
}