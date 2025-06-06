package com.example.smartee.ui.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDropdown(
    regionData: Map<String, List<String>>,
    selectedSido: String,
    selectedSigungu: String,
    onSidoSelected: (String) -> Unit,
    onSigunguSelected: (String) -> Unit
) {
    var sidoExpanded by remember { mutableStateOf(false) }
    var sigunguExpanded by remember { mutableStateOf(false) }

    val sigunguList = regionData[selectedSido] ?: emptyList()

    Column {
        // 시/도 드롭다운
        ExposedDropdownMenuBox(
            expanded = sidoExpanded,
            onExpandedChange = { sidoExpanded = !sidoExpanded }
        ) {
            TextField(
                value = selectedSido,
                onValueChange = {},
                readOnly = true,
                label = { Text("시/도 선택") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sidoExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = sidoExpanded,
                onDismissRequest = { sidoExpanded = false }
            ) {
                regionData.keys.sorted().forEach { sido ->
                    DropdownMenuItem(
                        text = { Text(sido) },
                        onClick = {
                            onSidoSelected(sido)
                            sidoExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 시군구 드롭다운
        ExposedDropdownMenuBox(
            expanded = sigunguExpanded,
            onExpandedChange = { sigunguExpanded = !sigunguExpanded }
        ) {
            TextField(
                value = selectedSigungu,
                onValueChange = {},
                readOnly = true,
                label = { Text("시/군/구 선택") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sigunguExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = sigunguExpanded,
                onDismissRequest = { sigunguExpanded = false }
            ) {
                sigunguList.forEach { sigungu ->
                    DropdownMenuItem(
                        text = { Text(sigungu) },
                        onClick = {
                            onSigunguSelected(sigungu)
                            sigunguExpanded = false
                        }
                    )
                }
            }
        }
    }
}
