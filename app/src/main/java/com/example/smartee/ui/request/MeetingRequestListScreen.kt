
package com.example.smartee.ui.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartee.model.MeetingJoinRequest
import com.example.smartee.viewmodel.MeetingRequestViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRequestListScreen(
    meetingId: String,
    navController: NavController
) {
    val viewModel: MeetingRequestViewModel = viewModel()
    val requests by viewModel.requests.collectAsState()

    // [추가] 스낵바를 위한 상태 및 스코프
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // [추가] ViewModel의 UI 이벤트를 구독하여 스낵바 표시
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is MeetingRequestViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    LaunchedEffect(meetingId) {
        viewModel.loadRequests(meetingId)
    }

    Scaffold(
        // [추가] 스낵바 호스트 설정
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("모임 참여 신청 목록") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("대기 중인 신청이 없습니다.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(requests) { request ->
                    RequestItem(
                        request = request,
                        onApprove = { viewModel.approveRequest(request) },
                        onReject = { viewModel.rejectRequest(request) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: MeetingJoinRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${request.requesterNickname} 님의 신청")
            Row {
                Button(onClick = onApprove) {
                    Text("승인")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onReject) {
                    Text("거절")
                }
            }
        }
    }
}