// smartee/ui/profile/ProfileContent.kt

package com.example.smartee.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.smartee.model.UserData
import com.example.smartee.ui.profile.Resource.ResourcesCard
import com.example.smartee.viewmodel.BadgeViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    currentUser: FirebaseUser?,
    userData: UserData?
) {
    // ▼▼▼ 뱃지 팝업을 제어할 상태 변수 추가 ▼▼▼
    var showBadgeDialog by remember { mutableStateOf(false) }

    // ▼▼▼ 뱃지 팝업이 보일 때 실행될 Composable ▼▼▼
    if (showBadgeDialog) {
        BadgeDialog(onDismiss = { showBadgeDialog = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader(currentUser = currentUser, userData = userData)
        Spacer(modifier = Modifier.height(24.dp))

        userData?.region?.let { location ->
            if (location.isNotEmpty()) {
                LocationCard(location = location)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        ResourcesCard(
            inkLevel = userData?.ink ?: 0,
            penCount = userData?.pen ?: 0
        )
        Spacer(modifier = Modifier.height(24.dp))

        InterestsCard(interests = userData?.interests ?: emptyList())

        // ▼▼▼ '활동 배지' 메뉴 아이템 추가 ▼▼▼
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        ProfileMenuItem(
            icon = Icons.Default.WorkspacePremium,
            text = "활동 배지",
            onClick = { showBadgeDialog = true } // 클릭 시 팝업 상태를 true로 변경
        )
        Divider()
    }
}

// ▼▼▼ 뱃지 팝업(Dialog) Composable 추가 ▼▼▼
@Composable
fun BadgeDialog(
    onDismiss: () -> Unit,
    viewModel: BadgeViewModel = viewModel()
) {
    val badges by viewModel.badges.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.loadBadges()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp), // 팝업의 최대 높이 지정
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("활동 배지", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                if (badges.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(badges) { badge ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = if (badge.isUnlocked) badge.unlockedImageUrl else badge.lockedImageUrl,
                                    contentDescription = badge.name,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .alpha(if (badge.isUnlocked) 1f else 0.3f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    badge.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

// ▼▼▼ 재사용 가능한 메뉴 아이템 Composable (ProfileScreen.kt에서 이동) ▼▼▼
@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "이동",
        )
    }
}


@Composable
fun NotLoggedInContent(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    // ... (기존 NotLoggedInContent 와 동일)
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