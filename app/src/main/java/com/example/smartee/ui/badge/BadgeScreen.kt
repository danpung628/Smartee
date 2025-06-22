// smartee/ui/badge/BadgeScreen.kt
package com.example.smartee.ui.badge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.smartee.viewmodel.BadgeViewModel

// 데이터 클래스를 파일 외부 또는 별도의 파일로 관리하는 것이 좋습니다.
data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val unlockedImageUrl: String = "",
    val lockedImageUrl: String = "",
    var isUnlocked: Boolean = false
)

@Composable
fun BadgeScreen(
    viewModel: BadgeViewModel = viewModel()
) {
    val badges by viewModel.badges.collectAsState()
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(key1 = Unit) {
        viewModel.loadBadges()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("활동 배지", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (badges.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(badges) { badge ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(enabled = badge.isUnlocked) { selectedBadge = badge }
                    ) {
                        AsyncImage(
                            model = badge.imageUrl,
                            contentDescription = badge.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .alpha(if (badge.isUnlocked) 1f else 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            badge.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    if (selectedBadge != null) {
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            title = { Text(selectedBadge!!.name) },
            text = { Text(selectedBadge!!.description) },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) { Text("확인") }
            }
        )
    }
}