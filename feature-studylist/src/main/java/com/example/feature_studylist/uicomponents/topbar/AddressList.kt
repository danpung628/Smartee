package com.example.feature_studylist.uicomponents.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AddressList(modifier: Modifier = Modifier) {
    val addressList = mutableListOf("군자동", "가락동", "잠실동")
    var selectedAddress by remember { mutableStateOf(addressList.first()) }
    var expanded by remember { mutableStateOf(false) }

    TextButton(
        onClick = {
            expanded = true
        }
    ) {
        Text(selectedAddress)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "지역 선택"
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        }
    ) {
        addressList.forEach {
            DropdownMenuItem(
                text = {
                    Text(it)
                },
                onClick = {

                }
            )
        }
    }
}

@Preview
@Composable
private fun AddressListPreview() {
    AddressList()
}