package ntu26.ss.parkinpeace.android.views

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import ntu26.ss.parkinpeace.android.R
import ntu26.ss.parkinpeace.android.viewmodels.SearchViewModel
import ntu26.ss.parkinpeace.android.viewmodels.SettingsViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsSearchColumn(type: String, id: Int) {
    val searchViewModel = viewModel<SearchViewModel>()
    val settingsViewModel = viewModel<SettingsViewModel>()
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
            //.weight(1f)
        ) {
            items(searchViewModel.searchResults) { req ->
                val carpark = req()
                SearchResultItem(location = carpark, onClick = {
                    settingsViewModel.setSelectedLocation(true)
                    when (type) {
                        "HOME" -> {
                            settingsViewModel.editHomeAddress(carpark.address)
                            settingsViewModel.setHomeLocation(carpark)
                        }

                        "WORK" -> {
                            settingsViewModel.editWorkAddress(carpark.address)
                            settingsViewModel.setWorkLocation(carpark)
                        }

                        "OTHER" -> {
                            settingsViewModel.editOtherAddress(id, carpark.address)
                            settingsViewModel.setOtherLocation(id, carpark)

                        }
                    }
                    searchViewModel.clearText()
                    keyboardController?.hide()
                })
            }
        }
    }
}

@Composable
fun SettingsSearchBar(type: String, id: Int, modifier: Modifier) {
    val searchViewModel = viewModel<SearchViewModel>()
    val settingsViewModel = viewModel<SettingsViewModel>()
    val searchText by searchViewModel.query.collectAsState()
    val selectedLocation by settingsViewModel.selectedLocation.collectAsState()
    val homeData by settingsViewModel.homeData.collectAsState()
    val workData by settingsViewModel.workData.collectAsState()
    val itemAddress = settingsViewModel.getOtherAddress(id)
    var typedAddress by remember {
        mutableStateOf("")
    }
    LaunchedEffect(selectedLocation)
    {
        when (type) {
            "HOME" -> if (homeData.address != "")
                typedAddress = homeData.address

            "WORK" -> if (workData.address != "")
                typedAddress = workData.address

            "OTHER" -> if (itemAddress != "")
                typedAddress = itemAddress
        }
    }

    SettingsSearchBox(
        modifier = Modifier.fillMaxWidth(),
        hintText = "Search for a location",
        query = typedAddress,
        onChange = {
            typedAddress = it
            searchViewModel.onSearchTextChange(text = typedAddress)
        },
        onFocusChanged = { searchViewModel.setFocused(it.isFocused) },
    )
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DialogBox(
    type: String,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val searchViewModel = viewModel<SearchViewModel>()
    val settingsViewModel = viewModel<SettingsViewModel>()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val homeData by settingsViewModel.homeData.collectAsState()
    val workData by settingsViewModel.workData.collectAsState()
    var typedName by remember {
        mutableStateOf("")
    }
    when (type) {
        "HOME" -> if (homeData.name != "") typedName =
            homeData.name

        "WORK" -> if (workData.name != "") typedName =
            workData.name
    }

    Dialog(
        onDismissRequest = {
            viewModel.onDismissDialog()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Text(
                    text = type,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
                Row()
                {
                    SettingsNameBox(
                        modifier = Modifier.fillMaxWidth(),
                        hintText = "Change Custom Name of location",
                        query = typedName,
                        onChange = { typedName = it },
                        onFocusChanged = {}
                    )
                    /*
                    TextField(
                        value = typedName,
                        placeholder = { Text(text = "Change Custom Name of location") },
                        onValueChange = { typedName = it },
                        modifier = modifier.fillMaxWidth()
                    )
                     */

                }
                Row()
                {
                    SettingsSearchBar(type, 0, modifier = modifier)
                }
                if (isSearching) {
                    SettingsSearchColumn(type, 0)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            when (type) {
                                "HOME" -> viewModel.editHomeName(typedName)
                                "WORK" -> viewModel.editWorkName(typedName)
                            }
                            viewModel.setSelectedLocation(false);
                            viewModel.onDismissDialog()
                        }
                    ) {
                        Text(
                            text = "Save".uppercase(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtherDialog(
    index: Int,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val searchViewModel = viewModel<SearchViewModel>()
    val settingsViewModel = viewModel<SettingsViewModel>()
    val isSearching by searchViewModel.isSearching.collectAsState()
    var typedName by remember {
        mutableStateOf("")
    }
    if (settingsViewModel.getOtherName(index) != "") {
        typedName = settingsViewModel.getOtherName(index)
    }

    Dialog(
        onDismissRequest = {
            viewModel.onDismissDialog()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Text(
                    text = "OTHER",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
                Row()
                {
                    SettingsNameBox(
                        modifier = Modifier.fillMaxWidth(),
                        hintText = "Change Custom Name of location",
                        query = typedName,
                        onChange = { typedName = it },
                        onFocusChanged = {}
                    )
                }
                Row()
                {
                    SettingsSearchBar(type = "OTHER", id = index, modifier = modifier)
                }
                if (isSearching) {
                    SettingsSearchColumn(type = "OTHER", id = index)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            viewModel.editOtherName(
                                index,
                                typedName
                            );
                            viewModel.setSelectedLocation(false);
                            viewModel.onDismissDialog()
                        },
                    ) {
                        Text(
                            text = "Save".uppercase(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun CardIcon(@DrawableRes id: Int) {
    Card(shape = CircleShape) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .padding(8.dp)
        ) {
            Icon(painterResource(id = id), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun LocationItemResult(@DrawableRes icon: Int, label: String, address: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp, 16.dp), verticalAlignment = Alignment.CenterVertically
    )
    {
        CardIcon(id = icon)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label.ifBlank { "Click to add custom location" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = address,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationList(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    var clickedLocation by remember {
        mutableStateOf("")
    }
    val homeData by viewModel.homeData.collectAsState()
    val workData by viewModel.workData.collectAsState()
    val otherData by viewModel.otherData.collectAsState()

    LaunchedEffect(null) {
        viewModel.setupInitial()
    }

    LazyColumn(modifier = Modifier.fillMaxWidth())
    {

        item {
            Card(
                onClick = {
                    clickedLocation = "HOME";
                    viewModel.onLocClick()
                },
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                LocationItemResult(icon = R.drawable.home, label = homeData.name, address = homeData.address)
            }
        }

        item {
            Card(
                onClick = {
                    clickedLocation = "WORK";
                    viewModel.onLocClick()
                },
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                LocationItemResult(icon = R.drawable.work, label = workData.name, address = workData.address)
            }
        }

        items(items = otherData, itemContent = { item ->
            Card(
                onClick = {
                    clickedLocation = "OTHER";
                    viewModel.onOtherClick();
                    viewModel.changeShownOther(item.id)
                },
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            )
            {
                LocationItemResult(icon = R.drawable.other, label = item.name, address = item.address)
            }
            if (viewModel.isOtherShown && viewModel.getShownOther() == item.id) {
                OtherDialog(index = item.id, viewModel = viewModel)
            }
        })

        item {
            OutlinedButton(
                onClick = { viewModel.addLocation("", "") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add New Location")
            }
        }
    }
    if (viewModel.isDialogShown) {
        when (clickedLocation) {
            "HOME" -> DialogBox("HOME", viewModel = viewModel)
            "WORK" -> DialogBox("WORK", viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUI(modifier: Modifier = Modifier) {
    val viewModel = viewModel<SettingsViewModel>()
    val sliderPosition by viewModel.sliderPosition.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    val selectedOptionText by viewModel.selectedOptionText.collectAsState()
    Column(modifier.fillMaxSize())
    {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        {
            Text(
                text = "Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            )
            Divider(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp), color = Color.Gray
            )
        }
        Column(modifier = modifier.weight(2f))
        {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vehicle Type",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
                Row(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = it },
                        modifier = Modifier.size(190.dp)
                    )
                    {
                        TextField(
                            readOnly = true,
                            value = selectedOptionText,
                            onValueChange = {},
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Car") },
                                onClick = {
                                    viewModel.editSelectedOptionText("Car")
                                    isExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Motorcycle") },
                                onClick = {
                                    viewModel.editSelectedOptionText("Motorcycle")
                                    isExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Heavy Vehicle") },
                                onClick = {
                                    viewModel.editSelectedOptionText("Heavy Vehicle")
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Search Distance",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "${sliderPosition.toInt()} m",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Slider(
                    value = sliderPosition,
                    onValueChange = { viewModel.editSliderPos(it) },
                    valueRange = viewModel.getSliderLowerBound()..viewModel.getSliderUpperBound(),
                    steps = 3,
                    modifier = Modifier
                        .size(350.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }


        // Scrollable List with Cards
        // Create Dialogs that appear when clicking any of the cards
        Column(
            modifier
                .weight(3.7f)
                .fillMaxWidth()
        )
        {
            Spacer(modifier = modifier.height(15.dp))
            Divider(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp), color = Color.Gray
            )
            LocationList(viewModel)
        }

    }
}

@Composable
fun SettingsSearchBox(
    modifier: Modifier = Modifier,
    hintText: String,
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
            placeholder = { Text(hintText) },
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
                    .onFocusChanged(onFocusChanged1)
            )
        )
    }
}

@Composable
fun SettingsNameBox(
    modifier: Modifier = Modifier,
    hintText: String,
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
            placeholder = { Text(hintText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            leadingIcon = {
                when {
                    query.isNotEmpty() || focused -> Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.clickable { onClear() })
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
                    .onFocusChanged(onFocusChanged1)
            )
        )
    }
}