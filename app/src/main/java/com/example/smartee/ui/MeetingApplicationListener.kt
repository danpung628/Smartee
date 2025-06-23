package com.example.smartee.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.viewmodel.MeetingNotificationViewModel

@Composable
fun MeetingApplicationListener() {
    val context = LocalContext.current
    val viewModel: MeetingNotificationViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.listenForApplications { meeting ->
            Toast.makeText(
                context,
                "미팅 '${meeting.title}'에 새 참가 신청이 있습니다!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
