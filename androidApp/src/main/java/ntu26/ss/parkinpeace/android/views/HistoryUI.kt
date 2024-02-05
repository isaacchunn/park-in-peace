package ntu26.ss.parkinpeace.android.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.maps.MapboxExperimental
import ntu26.ss.parkinpeace.android.models.StoredLocation
import ntu26.ss.parkinpeace.android.utils.LocalNavHostController
import ntu26.ss.parkinpeace.android.utils.Route
import ntu26.ss.parkinpeace.android.utils.navigate
import ntu26.ss.parkinpeace.android.viewmodels.HistoryViewModel

@Composable
fun HistoryUI(modifier: Modifier = Modifier) {

    val historyViewModel = viewModel<HistoryViewModel>()

    LaunchedEffect(key1 = 1, block = {
        historyViewModel.getSearchHistory()
        historyViewModel.getHistory()
    })

    Column(
        modifier
            .offset(y = 150.dp)
            .padding(start = 20.dp)
    ) {
        RecentVisited()
        Spacer(modifier = modifier.height(10.dp))
        RecentSearch()
    }
    SearchUI()
}

@Composable
fun RecentVisited(modifier: Modifier = Modifier) {
    val historyViewModel = viewModel<HistoryViewModel>()
    val history by historyViewModel.state.collectAsState()
    Box {
        if(history.isEmpty()) Text(text = "Empty! Visit a carpark now!", modifier.align(Alignment.Center))
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = modifier
                            .offset(y = 3.dp)
                    )
                    Column(
                        modifier = modifier.padding(start = 10.dp)
                    ) {
                        Text(
                            text = "Visited",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "From Your Location History",
                            fontSize = 15.sp,
                        )
                    }
                }
                IconWithPopup(
                    name = "Clear Location History",
                    vector = Icons.Filled.Delete,
                    onDeleteConfirmed = { historyViewModel.purgeLocationHistory() },
                    modifier = modifier
                        .padding(end = 20.dp, top = 5.dp)
                )
            }
            LazyColumn(
                modifier
                    .height(180.dp)
                    .padding(top = 10.dp, start = 10.dp, end = 20.dp)
            ) {
                items(items = history, itemContent = { item ->
                    Surface(
                        shadowElevation = 3.dp,
                    ) {
                        RecentVisitedBar(item)
                        BottomShadow()
                    }
                })
            }
        }
    }
}

@Composable
fun RecentVisitedBar(location: StoredLocation, modifier: Modifier = Modifier) {
    val viewModel = viewModel<HistoryViewModel>()
    val navigation = LocalNavHostController.current
    Box(
        modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, top = 5.dp)
            .height(70.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                navigation.navigate(
                    Route.Explore(
                        location.epsg4326
                    )
                )
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
        ) {
            Column(
                modifier
                    .padding(start = 15.dp)
            ) {
                Text(
                    text = if (location.name.length > 30) "${location.name.take(30)}..." else location.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = if (location.address.length > 40) "${location.address.take(40)}..." else location.address,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconWithPopup(
                name = "Delete Location",
                vector = Icons.Filled.MoreVert,
                onDeleteConfirmed = { viewModel.deleteVisitedHistory(location) })
        }
    }
}

@Composable
fun RecentSearch(modifier: Modifier = Modifier) {
    val historyViewModel = viewModel<HistoryViewModel>()
    val searchHistory by historyViewModel.recentSearch.collectAsState()
    Box {
        if(searchHistory.isEmpty()) Text(text = "Empty! Search for a Location now!", modifier.align(
            Alignment.Center))
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = modifier
                            .offset(y = 3.dp)
                    )
                    Text(
                        text = "Your Recent Searches",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = modifier.padding(start = 10.dp)
                    )
                }
                IconWithPopup(
                    name = "Clear Search History",
                    vector = Icons.Filled.Delete,
                    onDeleteConfirmed = { historyViewModel.purgeSearchHistory() },
                    modifier = modifier
                        .padding(end = 20.dp, top = 5.dp)
                )
            }
            LazyColumn(
                modifier
                    .height(200.dp)
                    .padding(top = 10.dp, start = 10.dp, end = 20.dp)
            ) {
                items(items = searchHistory, itemContent = { item ->
                    Surface(
                        shadowElevation = 3.dp,
                    ) {
                        RecentSearchBar(item)
                        BottomShadow()
                    }
                })
            }
        }
    }
}

@Composable
fun RecentSearchBar(location: StoredLocation, modifier: Modifier = Modifier) {
    val viewModel = viewModel<HistoryViewModel>()
    val navigation = LocalNavHostController.current
    Box(
        modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, top = 5.dp)
            .height(70.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                navigation.navigate(
                    Route.Explore(
                        location.epsg4326
                    )
                )
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
        ) {
            Column(
                modifier
                    .padding(start = 15.dp)
            ) {
                Text(
                    text = if (location.name.length > 30) "${location.name.take(30)}..." else location.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = if (location.address.length > 40) "${location.address.take(40)}..." else location.address,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconWithPopup(
                name = "Delete Search",
                vector = Icons.Filled.MoreVert,
                onDeleteConfirmed = { viewModel.deleteSearchHistory(location) })
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun HistoryInfoBar(location: StoredLocation, modifier: Modifier = Modifier) {
    val coordinate = location.epsg4326
    val navigation = LocalNavHostController.current
    Column {
        Box(
            modifier
                .height(150.dp)
                .width(220.dp)
                .padding(top = 15.dp, bottom = 15.dp, end = 25.dp)
                .clip(shape = RoundedCornerShape(20.dp))
        ) {
            MapboxV2(
                modifier = modifier,
                camera = coordinate,
                destination = coordinate,
                mapboxLogo = false,
                offsetAttribution = false,
                animateCamera = false,
                enabled = false
            )
        }
        Text(
            text = location.name.take(18) + "...",
            modifier = modifier
                .padding(start = 15.dp)
                .clickable { navigation.navigate(Route.Explore(coordinate)) },
            color = Color.Blue,
            style = TextStyle(
                textDecoration = TextDecoration.Underline // You can use other TextDecoration values as well
            )
        )
    }
}

@Composable
fun IconWithPopup(
    name: String,
    vector: ImageVector,
    modifier: Modifier = Modifier,
    onDeleteConfirmed: () -> Unit
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    Icon(
        imageVector = vector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(end = 15.dp)
            .clickable { isDialogVisible = true }
    )

    if (isDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDialogVisible = false },
            title = { Text("$name ?") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                IconButton(
                    onClick = {
                        onDeleteConfirmed()
                        isDialogVisible = false
                    }
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Delete")
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        isDialogVisible = false
                    }
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = "Delete")
                }
            }
        )
    }
}

@Composable
fun BottomShadow(alpha: Float = 0.1f, height: Dp = 6.dp, modifier: Modifier = Modifier) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                    Color.Transparent,
                )
            )
        )
    )
}
