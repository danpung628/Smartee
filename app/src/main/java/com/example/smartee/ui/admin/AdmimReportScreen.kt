// com.example.smartee.ui.admin.AdminReportScreen.kt

package com.example.smartee.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.viewmodel.UserViewModel
import com.example.smartee.viewmodel.UserViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

data class Report(
    val studyId: String = "",
    val reason: String = "",
    val timestamp: Long = 0L,
    val studyTitle: String = ""  // ✅ 스터디 제목 추가
)

@Composable
fun AdminReportScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val context = LocalContext.current
    val userData by userViewModel.userData.observeAsState()

    if (userData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isAdmin = userData?.nickname?.contains("admin", ignoreCase = true) == true

    if (!isAdmin) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("관리자 권한이 없습니다.")
        }
        return
    }

    var reports by remember { mutableStateOf(listOf<Pair<String, Report>>()) }

    fun loadReports() {
        FirebaseFirestore.getInstance().collection("reports")
            .get()
            .addOnSuccessListener { snapshot ->
                reports = snapshot.documents.mapNotNull { doc ->
                    val report = doc.toObject(Report::class.java)
                    if (report != null) doc.id to report else null
                }
            }
    }

    LaunchedEffect(Unit) {
        loadReports()
    }

    Column(Modifier.padding(16.dp)) {
        Text("신고 목록", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(reports) { (docId, report) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("스터디 제목: ${report.studyTitle}", style = MaterialTheme.typography.titleMedium)
                        Text("스터디 ID: ${report.studyId}", style = MaterialTheme.typography.bodySmall)
                        Text("신고 사유: ${report.reason}")
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // ✅ 무시하기: 신고 문서만 삭제
                                    FirebaseFirestore.getInstance().collection("reports").document(docId).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "신고 무시됨", Toast.LENGTH_SHORT).show()
                                            loadReports()
                                        }
                                }
                            ) {
                                Text("무시하기")
                            }

                            OutlinedButton(
                                onClick = {
                                    // ✅ 스터디 삭제 + 신고 문서 삭제
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("studies").document(report.studyId).delete()
                                        .addOnSuccessListener {
                                            db.collection("reports").document(docId).delete()
                                            Toast.makeText(context, "스터디 및 신고 삭제 완료", Toast.LENGTH_SHORT).show()
                                            loadReports()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "스터디 삭제 실패", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            ) {
                                Text("스터디 삭제")
                            }
                        }
                    }
                }
            }
        }
    }
}
