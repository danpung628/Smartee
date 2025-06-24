// smartee/ui/request/RequestListScreen.kt

package com.example.smartee.ui.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.model.JoinRequest
import com.example.smartee.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestListScreen(
    navController: NavController,
    // [수정] studyId를 받지 않으므로 ViewModel을 직접 생성
    viewModel: RequestViewModel = viewModel()
) {
    val requests by viewModel.requests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // [수정] studyId 없이 현재 사용자의 모든 요청을 로드
    LaunchedEffect(Unit) {
        viewModel.loadRequestsForCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스터디 가입 요청") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("수신된 가입 요청이 없습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests) { request ->
                    RequestItem(
                        request = request,
                        onApprove = { viewModel.approveRequest(it) },
                        onReject = { viewModel.rejectRequest(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: JoinRequest,
    onApprove: (JoinRequest) -> Unit,
    onReject: (JoinRequest) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("신청자: ${request.requesterNickname}", style = MaterialTheme.typography.titleMedium)
            Text("스터디: ${request.studyTitle}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onApprove(request) }) {
                    Text("수락")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { onReject(request) }) {
                    Text("거절")
                }
            }
        }
    }
}