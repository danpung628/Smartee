package com.example.smartee.ui.study.studyList.studydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartee.model.StudyData

@Composable
fun StudyScheduleInfo(study: StudyData) {
    Text(
        text = "스터디 일정",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 스터디 기간
            InfoRow(
                icon = Icons.Default.DateRange,
                label = "스터디 기간:",
                value = "${study.startDate} ~ ${study.endDate}"
            )

            // 스터디 유형 (정기/비정기)
            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "스터디 유형:",
                value = if (study.isRegular) "정기 스터디" else "비정기 스터디"
            )

            // 정기 스터디 정보
            if (study.isRegular && study.regularDays.isNotEmpty()) {
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "정기 일정:",
                    value = study.regularDays.joinToString(", ") + " " + study.regularTime
                )
            }

            // 비정기 스터디 정보
            if (!study.isRegular && study.irregularDateTimes.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(start = 28.dp, top = 4.dp)
                ) {
                    Text(
                        text = "비정기 일정:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    study.irregularDateTimes.forEach { dateTime ->
                        Text(
                            text = "• $dateTime",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                        )
                    }
                }
            }

            // 오프라인 여부
            InfoRow(
                icon = if (study.isOffline) Icons.Default.LocationOn else Icons.Default.Computer,
                label = "진행 방식:",
                value = if (study.isOffline) "오프라인" else "온라인"
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}