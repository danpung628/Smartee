package com.example.smartee.ui.study.studyList.topbar.address

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun AddressList(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel,
    onSelectAddress: (String) -> Unit
) {
    var selectedAddress = studyViewModel.selectedAddress
    var expanded = studyViewModel.addressExpanded

    TextButton(
        onClick = {
            studyViewModel.addressExpanded = true
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
            studyViewModel.addressExpanded = false
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
                    studyViewModel.addressExpanded = false
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