package com.example.smartee.ui.study.editstudy.ui.component

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.util.Calendar

@Composable
fun DatePickerField(label: String, date: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = date?.year ?: calendar.get(Calendar.YEAR)
    val month = date?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
    val day = date?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

    val dateString = date?.toString() ?: ""

    OutlinedTextField(
        value = dateString,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(context, { _: DatePicker, y: Int, m: Int, d: Int ->
                    onDateSelected(LocalDate.of(y, m + 1, d))
                }, year, month, day).show()
            }
    )
}
