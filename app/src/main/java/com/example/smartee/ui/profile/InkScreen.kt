package com.example.smartee.ui.ink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun InkScreen() {
    val expanded = remember { mutableStateOf(false) }
    var inkPercent = 82
    val recentChange = "+5 환급됨"

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
                    Text("$inkPercent%", fontSize = 20.sp, color = Color(0xFF2196F3))
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { inkPercent / 100f },
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
    InkScreen()
}