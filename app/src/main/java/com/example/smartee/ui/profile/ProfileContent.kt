package com.example.smartee.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartee.model.UserProfile
import com.example.smartee.ui.profile.Resource.ResourcesCard
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    currentUser: FirebaseUser?,
    userProfile: UserProfile?
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 헤더 (이미지, 이름, 이메일)
        ProfileHeader(currentUser = currentUser)

        Spacer(modifier = Modifier.height(24.dp))

        // 지역 정보 (있는 경우에만)
        userProfile?.location?.let { location ->
            if (location.isNotEmpty()) {
                LocationCard(location = location)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 자원 현황 (잉크, 만년필)
        ResourcesCard(
            inkLevel = userProfile?.inkLevel ?: 0,
            penCount = userProfile?.penCount ?: 0
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 관심 카테고리
        InterestsCard(interests = userProfile?.interests ?: emptyList())
    }
}

@Composable
fun NotLoggedInContent(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("로그인이 필요합니다")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLoginClick) {
            Text("로그인 하러 가기")
        }
    }
}