package com.example.smartee.ui.ink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InkScreen(inkLevel: Int = 50) {  // 기본값 설정, 실제 값은 매개변수로 받음
    val expanded = remember { mutableStateOf(false) }
    val recentChange = "+5 환급됨"  // 이 부분도 실제 데이터로 대체해야 함

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // 잉크 상태 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("잉크", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("$inkLevel%", fontSize = 20.sp, color = Color(0xFF2196F3))  // 실제 잉크 레벨 표시
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { inkLevel / 100f },  // 실제 잉크 레벨로 계산
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF2196F3),
                    trackColor = Color.LightGray,
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("최근 변경: $recentChange", fontSize = 14.sp, color = Color.Gray)
            }
        }

        //  설명 접기/펼치기
        OutlinedButton(onClick = { expanded.value = !expanded.value }) {
            Text(if (expanded.value) "잉크란? (접기)" else "잉크란? (펼치기)")
        }

        AnimatedVisibility(visible = expanded.value) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    "잉크는 사용자의 신뢰도와 활동을 포인트로 시각화한 시스템입니다.\n" +
                            "스터디 참가 시 보증금처럼 차감되며, 성실한 참여를 통해 환급되거나\n" +
                            "특정 권한(스터디 개설, 테마 변경, 등급 등)에 영향을 줍니다.",
                    fontSize = 15.sp
                )
            }
        }
    }
}


@Composable
fun Bullet(text: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text("• ", fontSize = 16.sp)
        Text(text, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun InkScreenPreview() {
//    InkScreen(userProfile?.inkLevel ?: 50)
}