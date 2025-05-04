package com.example.feature_studylist.uicomponents.topbar.address

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature_studylist.viewmodel.AddressViewModel

@Composable
fun AddressList(
    modifier: Modifier = Modifier,
    viewModel: AddressViewModel = viewModel()
) {
    val addresses by viewModel.addresses.collectAsState()
    var selectedAddress by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // 컴포넌트가 처음 로드될 때 주소 목록 가져오기
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchSidoList()
    }

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
                    expanded = false
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