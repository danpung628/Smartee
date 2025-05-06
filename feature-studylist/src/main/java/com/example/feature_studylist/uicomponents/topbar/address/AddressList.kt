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
import com.example.feature_studylist.viewmodel.StudyViewModel

@Composable
fun AddressList(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel,
    onSelectAddress: (String) -> Unit
) {
    var selectedAddress = ""
    var expanded by remember { mutableStateOf(false) }

    TextButton(
        onClick = {
            expanded = true
        }
    ) {
        Text(
            if (selectedAddress == "")
                "지역 선택"
            else
                selectedAddress
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select Address"
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        }
    ) {
        studyViewModel.addressList.forEach {
            DropdownMenuItem(
                text = {
                    Text(
                        if (it == "")
                            "지역 선택"
                        else
                            it
                    )
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