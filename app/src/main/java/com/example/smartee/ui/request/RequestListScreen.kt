package com.example.smartee.ui.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.model.JoinRequest
import com.example.smartee.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestListScreen(studyId: String) {
    val viewModel: RequestViewModel = viewModel()
    val requests by viewModel.requests.collectAsState()

    LaunchedEffect(key1 = studyId) {
        viewModel.loadRequests(studyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("가입 요청 관리") })
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            if (requests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("새로운 가입 요청이 없습니다.")
                    }
                }
            } else {
                items(requests) { request ->
                    RequestItem(
                        request = request,
                        onApprove = { viewModel.approveRequest(request, studyId) },
                        onReject = { viewModel.rejectRequest(request, studyId) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: JoinRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${request.requesterNickname} 님의 가입 요청", modifier = Modifier.weight(1f))
            Row {
                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("수락")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onReject) {
                    Text("거절")
                }
            }
        }
    }
}