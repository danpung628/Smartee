package com.example.smartee.ui.signup

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartee.model.UserData
import com.example.smartee.navigation.Screen
import com.example.smartee.ui.common.LoadingOverlay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FillProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: return
    val email = user.email ?: ""
    val name = user.displayName ?: ""
    val photoUrl = user.photoUrl?.toString() ?: ""

    var nickname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("남성") }
    var region by remember { mutableStateOf("") }

    val interestsList = listOf("스터디", "운동", "영어", "자격증", "취미")
    val selectedInterests = remember { mutableStateMapOf<String, Boolean>() }
    interestsList.forEach { selectedInterests.putIfAbsent(it, false) }

    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("프로필 입력", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("닉네임") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
                label = { Text("나이") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text("성별 선택")
            Row {
                listOf("남성", "여성").forEach {
                    Row(
                        Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == it,
                            onClick = { gender = it }
                        )
                        Text(it)
                    }
                }
            }

            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("거주 지역") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("관심 분야")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                interestsList.forEach { interest ->
                    val isSelected = selectedInterests[interest] == true
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                selectedInterests[interest] = !isSelected
                            },
                        color = if (isSelected) Color(0xFF6A4CBD) else Color(0xFFE6E1EC)
                    ) {
                        Text(
                            text = interest,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val ageInt = age.toIntOrNull()
                    if (ageInt == null || ageInt <= 0) {
                        Toast.makeText(context, "유효한 나이를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (nickname.trim().isEmpty() || age.isBlank() || region.isBlank()) {
                        Toast.makeText(context, "닉네임, 나이, 지역은 필수 입력 항목입니다.", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    if (selectedInterests.none { it.value }) {
                        Toast.makeText(context, "관심 분야를 최소 1개 이상 선택해주세요.", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    isLoading = true

                    val userData = UserData(
                        uid = uid,
                        email = email,
                        name = name,
                        photoUrl = photoUrl,
                        nickname = nickname,
                        age = age.toIntOrNull() ?: 0,
                        gender = gender,
                        region = region,
                        interests = selectedInterests.filterValues { it }.keys.toList(),
                        ink = 0,
                        pen = 0
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "프로필 저장 완료!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Login.route)
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "유저 정보 저장 실패: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장하고 시작하기")
            }
        }

    }
    if (isLoading) {
        LoadingOverlay()
    }
}