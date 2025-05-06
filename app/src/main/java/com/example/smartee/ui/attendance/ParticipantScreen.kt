package com.example.smartee.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ParticipantScreen(navController: NavController, correctCode: Int) {
    var codeInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("참여자 출석", fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            result = "출석되었습니다! (블루투스)"
        }) {
            Text("블루투스 출석")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("코드 입력") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            result = if (codeInput == correctCode.toString()) {
                "출석되었습니다! (코드 입력)"
            } else {
                "코드가 틀렸습니다."
            }
        }) {
            Text("코드로 출석")
        }

        result?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Text(it, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("← 돌아가기")
        }
    }
}
