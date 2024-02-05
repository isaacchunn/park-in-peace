package ntu26.ss.parkinpeace.android.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ntu26.ss.parkinpeace.android.R
import ntu26.ss.parkinpeace.android.models.StoredLocation
import ntu26.ss.parkinpeace.android.viewmodels.HistoryViewModel
import ntu26.ss.parkinpeace.android.viewmodels.MapViewModel
import ntu26.ss.parkinpeace.android.viewmodels.SearchViewModel
import ntu26.ss.parkinpeace.models.Location

@Composable
fun SearchUI() {
    val mapViewModel = viewModel<MapViewModel>()
    val viewModel = viewModel<SearchViewModel>()
    val searchText by viewModel.query.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val customLocations by mapViewModel.customLocations.collectAsState()

    if (isSearching) {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(88.dp))
                SearchResults()
            }
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = 28.dp)
            .padding(start = 16.dp)
    ) {
        Box(Modifier.padding(end = 16.dp)) {
            SearchBox(
                modifier = Modifier.fillMaxWidth(),
                query = searchText,
                onChange = viewModel::onSearchTextChange,
                onFocusChanged = { viewModel.setFocused(it.isFocused) })
        }
        if (!isSearching) {
            Spacer(Modifier.height(4.dp))
            StoredLocationChipGroup(
                data = customLocations,
                onChipSelected = mapViewModel::gotoLocation
            )
        }
    }
}

@Composable
fun dividerFun() {
    Divider(
        Modifier
            .fillMaxWidth()
            .height(1.dp), color = Color.Gray
    )
}

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    query: String,
    onChange: (String) -> Unit,
    onFocusChanged: (FocusState) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    val onFocusChanged1 = { it: FocusState -> focused = it.isFocused; onFocusChanged(it) }
    val onClear = { focusManager.clearFocus(); onChange("") }
    Surface(shape = RoundedCornerShape(100), shadowElevation = 4.dp) {
        TextField(
            query,
            onChange,
            placeholder = { Text("Heading somewhere?") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = {
                when {
                    query.isNotEmpty() || focused -> Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.clickable { onClear() })

                    else -> Icon(imageVector = Icons.Default.Search, contentDescription = null)
                }
            },
            shape = RoundedCornerShape(100),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = modifier.then(
                Modifier
                    .background(Color.Transparent)
                    .onFocusChanged(onFocusChanged1))
        )
    }
}

@Composable
fun StoredLocationChipGroup(data: List<StoredLocation>, onChipSelected: (Location) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items(data, key = { it.tag }) {
            StoredLocationChip(it, onClick = { onChipSelected(it) })
        }
        item { Spacer(Modifier.width(16.dp)) }
    }
}

@Composable
fun StoredLocationChip(data: StoredLocation, onClick: () -> Unit) {
    val icon = when (data.tag.lowercase()) {
        "home" -> painterResource(R.drawable.home)
        "work" -> painterResource(R.drawable.work)
        else -> painterResource(R.drawable.other)
    }
    ElevatedAssistChip(
        onClick = onClick,
        label = { Text(data.tag) },
        leadingIcon = { Icon(icon, data.tag) },
        shape = RoundedCornerShape(100)
    )
}

@Composable
fun SearchResultItem(location: Location, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()) {
            Surface(modifier = Modifier.padding(8.dp)) {
                Icon(
                    painter = painterResource(R.drawable.location_pin),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchResults() {
    val mapViewModel = viewModel<MapViewModel>()
    val historyViewModel = viewModel<HistoryViewModel>()
    val viewModel = viewModel<SearchViewModel>()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val onClick = { location: Location ->
        historyViewModel.updateSearchHistory(
            StoredLocation(
                tag = location.name,
                name = location.name,
                address = location.address,
                epsg4326 = location.epsg4326
            )
        )
        mapViewModel.gotoLocation(location.epsg4326)
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        items(viewModel.searchResults) { req ->
            val carpark = req()
            SearchResultItem(carpark, onClick = { onClick(carpark) })
        }
    }
}


