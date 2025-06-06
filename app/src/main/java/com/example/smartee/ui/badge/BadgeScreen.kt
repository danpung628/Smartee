import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartee.R

// 배지 데이터 모델
data class Badge(
    val title: String,
    val description: String,
    val imageResId: Int,
    val isUnlocked: Boolean = false // 🔐 잠금 여부
)

// 예시 배지 리스트 (이미지 리소스는 drawable에 준비 필요)
val badgeList = listOf(
    Badge("스터디왕", "스터디에 10번 참여했어요!", R.drawable.dumy, true),
    Badge("스터디 창조", "스터디를 10개 만들었어요!", R.drawable.dumy, false),
    Badge("잉크 부자", "잉크가 100을 넘었어요!", R.drawable.dumy, true),
    Badge("시간 지킴이", "출석률 100% 기록!", R.drawable.dumy, false),
    Badge("후기요정", "후기를 3개 이상 남겼어요!", R.drawable.dumy, true),
    Badge("첫 후기의 설렘", "첫 후기를 남겼어요!", R.drawable.dumy, true),
    Badge("단골 손님", "같은 스터디 3번 이상 참여!", R.drawable.dumy, false),
    Badge("잉크 첫 도달", "잉크 10 누적!", R.drawable.dumy, true),
    Badge("리더", "내가 만든 스터디에 3명 이상 참여", R.drawable.dumy, false),
    Badge("추천인", "다른 사람에게 앱 추천", R.drawable.dumy, false)
)

@Composable
fun BadgeScreen(badges: List<Badge>) {
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("활동 배지", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxHeight()) {
            items(badges.size) { index ->
                val badge = badges[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(enabled = badge.isUnlocked) { selectedBadge = badge }
                ) {
                    Image(
                        painter = painterResource(id = badge.imageResId),
                        contentDescription = badge.title,
                        modifier = Modifier.size(72.dp),
                        colorFilter = if (!badge.isUnlocked) ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) else null
                    )
                    Text(
                        badge.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }

    // 팝업 다이얼로그
    if (selectedBadge != null) {
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            title = { Text(selectedBadge!!.title) },
            text = { Text(selectedBadge!!.description) },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("확인")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBadgeScreen() {
    BadgeScreen(badges = badgeList.mapIndexed { i, badge ->
        badge.copy(
            imageResId = R.drawable.ic_launcher_foreground,
            isUnlocked = i % 2 == 0 // 테스트: 짝수 인덱스만 활성화
        )
    })
}