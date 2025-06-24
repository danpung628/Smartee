package com.example.smartee.ui.report

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ReportScreen(studyId: String, navController: NavController) {
    var reason by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("신고 사유를 입력해주세요", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { navController.popBackStack() }) {
                Text("← 뒤로가기")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("신고 사유") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (reason.isBlank() || isSubmitting) return@Button
                isSubmitting = true

                val db = FirebaseFirestore.getInstance()
                db.collection("studies").document(studyId).get()
                    .addOnSuccessListener { document ->
                        val studyTitle = document.getString("title") ?: "제목 없음"
                        val report = hashMapOf(
                            "studyId" to studyId,
                            "studyTitle" to studyTitle, // ✅ 제목 추가 저장
                            "reason" to reason,
                            "timestamp" to System.currentTimeMillis()
                        )
                        db.collection("reports").add(report)
                            .addOnSuccessListener {
                                Toast.makeText(context, "신고가 접수되었습니다", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "신고 등록 실패", Toast.LENGTH_SHORT).show()
                            }
                            .addOnCompleteListener { isSubmitting = false }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "스터디 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                        isSubmitting = false
                    }
            },
            enabled = !isSubmitting
        ) {
            Text(if (isSubmitting) "제출 중..." else "신고 제출")
        }
    }
}
