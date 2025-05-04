package com.example.feature_studylist.uicomponents.topbar.address

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
fun AddressList(
    modifier: Modifier = Modifier,
    onSelectAddress:(String)->Unit
) {
    val addresses = mutableListOf("군자동", "구의제3동", "휘경동")
    var selectedAddress = ""
    var expanded by remember { mutableStateOf(false) }


    TextButton(
        onClick = {
            expanded = true
        }
    ) {
        Text(selectedAddress.ifEmpty { "지역 선택" })
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
        addresses.forEach {
            DropdownMenuItem(
                text = {
                    Text(it)
                },
                onClick = {
                    selectedAddress = it
                    onSelectAddress(it)
                    expanded = false
                }
            )
        }
    }
}

@Preview
@Composable
private fun AddressListPreview() {
//    AddressList()
}