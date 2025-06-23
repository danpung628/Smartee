package com.example.smartee.ui.study.editstudy.ui.component

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

// [수정] Composable 전체적인 구조 변경
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier // [추가] 유연한 레이아웃을 위해 modifier 파라미터 추가
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // DatePickerDialog 생성
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // [수정] TextField 대신 OutlinedButton 사용
    OutlinedButton(
        onClick = { datePickerDialog.show() },
        modifier = modifier.height(56.dp) // TextField와 높이를 맞춤
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "$label 선택"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (date != null) {
                    // [추가] 날짜 포맷을 더 보기 좋게 변경
                    "$label: ${date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                } else {
                    "$label: 날짜 선택"
                },
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}