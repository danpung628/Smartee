import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

@Composable
fun AddressSearchBar(
    onSelectAddress: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    // 수정된 코드
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    Column {
        // 검색창
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                // 자동완성 요청
                if (it.isNotEmpty()) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(it)
                        .setTypeFilter(TypeFilter.REGIONS)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            suggestions = response.autocompletePredictions.map {
                                it.getPrimaryText(null).toString()
                            }
                        }
                }
            },
            label = { Text("지역 검색") },
            modifier = Modifier.fillMaxWidth()
        )

        // 자동완성 제안 목록
        LazyColumn {
            items(suggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            searchQuery = suggestion
                            onSelectAddress(suggestion)
                            suggestions = emptyList()
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}