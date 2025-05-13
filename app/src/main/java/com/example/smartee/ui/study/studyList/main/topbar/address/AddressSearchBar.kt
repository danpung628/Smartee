
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartee.viewmodel.AddressViewModel

@Composable
fun AddressSearchBar(
    addressViewModel: AddressViewModel = viewModel(),
    onSelectAddress: (String) -> Unit
) {
    Column {
        // 검색창
        OutlinedTextField(
            value = addressViewModel.addressSearchQuery,
            onValueChange = {
                addressViewModel.addressSearchQuery = it
                addressViewModel.searchAddresses(it)
            },
            label = { Text("지역 검색") },
        )

        // 자동완성 제안 목록
        LazyColumn {
            items(addressViewModel.addressSuggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .clickable {
                            addressViewModel.selectAddress(suggestion)
                            onSelectAddress(suggestion)
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}