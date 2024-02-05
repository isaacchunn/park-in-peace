package ntu26.ss.parkinpeace.android.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ntu26.ss.parkinpeace.android.R
import ntu26.ss.parkinpeace.android.models.*
import ntu26.ss.parkinpeace.android.utils.isHoliday
import ntu26.ss.parkinpeace.android.viewmodels.HistoryViewModel
import ntu26.ss.parkinpeace.android.viewmodels.MapViewModel
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.models.ParkingSystem
import ntu26.ss.parkinpeace.models.VehicleType
import java.text.NumberFormat
import kotlin.math.roundToInt

val currencyFormatter = NumberFormat.getCurrencyInstance()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarparkBottomSheet(
    modifier: Modifier = Modifier,
    availableCarparks: List<CarparkAvailability>,
    vehicleType: VehicleType,
    sortingStrategy: SortingStrategy,
    filters: NamedFilters,
    isOpen: Boolean = false,
    onDismissRequest: () -> Unit,
    onSortChanged: (SortingStrategy) -> Unit,
    onCarparkSelected: (CarparkAvailability) -> Unit,
    onFilterChanged: (NamedFilter) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var isFilterOpen by remember { mutableStateOf(false) }

    if (isOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            modifier = modifier.fillMaxSize()
        ) {

            Column {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Nearby Carparks", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(24.dp))
                    SortingStrategyControls(
                        sortingStrategy = sortingStrategy,
                        onSortChanged = onSortChanged,
                        onFilterOpen = { isFilterOpen = true })
                }
                NearbyCarparkList(availableCarparks, vehicleType, onCarparkSelected = onCarparkSelected)
            }

            FilterDialog(
                filters = filters,
                show = isFilterOpen,
                onFilterChanged = onFilterChanged,
                onDismiss = { isFilterOpen = false })

        }
    }
}

@Composable
fun SortingStrategyControls(
    sortingStrategy: SortingStrategy,
    onSortChanged: (SortingStrategy) -> Unit,
    onFilterOpen: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        item {
            IconButton(onClick = onFilterOpen) {
                Icon(
                    painterResource(R.drawable.filter),
                    contentDescription = "Open Filter Menu"
                )
            }
        }
        item { SortingStrategyModeChip(sortingStrategy, onSortChanged) }
        item { SortingStrategyOrderChip(sortingStrategy, onSortChanged) }
    }
}

@Composable
fun SortingStrategyModeChip(sortingStrategy: SortingStrategy, onSortChanged: (SortingStrategy) -> Unit) {
    AssistChip(onClick = { onSortChanged(sortingStrategy.cycleMode()) },
        label = { Text("Sort: ${sortingStrategy.mode.display}") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.sort),
                contentDescription = sortingStrategy.mode.display
            )
        })
}

@Composable
fun SortingStrategyOrderChip(sortingStrategy: SortingStrategy, onSortChanged: (SortingStrategy) -> Unit) {
    val icon = when (sortingStrategy.order) {
        SortingStrategy.Order.ASCENDING -> R.drawable.sort_up
        SortingStrategy.Order.DESCENDING -> R.drawable.sort_down
    }

    AssistChip(onClick = { onSortChanged(sortingStrategy.cycleOrder()) },
        label = { Text("Sort: ${sortingStrategy.order.display}") },
        leadingIcon = { Icon(painter = painterResource(icon), contentDescription = sortingStrategy.order.display) })

}

@Composable
fun NearbyCarparkList(
    carparks: List<CarparkAvailability>,
    vehicleType: VehicleType,
    onCarparkSelected: (CarparkAvailability) -> Unit
) {
    LazyColumn {
        items(carparks) {
            NearbyCarpark(it, vehicleType, onClick = { onCarparkSelected(it) })
        }
    }
}

@Composable
fun NearbyCarpark(carpark: CarparkAvailability, vehicleType: VehicleType, onClick: () -> Unit) {
    carpark.info ?: return
    val title = carpark.info.name
    val distance = carpark.distance?.let { "${(it.toDouble() / 1000).roundToInt()} km" } ?: "-"
    val travelTime = carpark.travelTime?.let { "$it min" } ?: "-"
    val lots = carpark.info.getApplicableLots(vehicleType, carpark.asof, carpark.asof.isHoliday())
    val price = lots.minPriceOrNull()?.let { currencyFormatter.format(it.rate.toDouble() / 100) } ?: "N/A"
    val vacantNow = carpark.lots[vehicleType]?.current?.let { if (it < 0) null else it }?.toString() ?: "-"
    val vacantPredicted = carpark.lots[vehicleType]?.predicted?.let { if (it < 0) null else it }?.toString() ?: "N/A"
    val agency = carpark.info.ref.split("/", limit = 2).firstOrNull()?.uppercase() ?: "N/A"
    Surface(
        onClick = onClick,
        border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Surface(modifier = Modifier.padding(8.dp)) {
                Icon(
                    painter = painterResource(R.drawable.carpark),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                        LocalTextStyle provides MaterialTheme.typography.labelLarge
                    ) {
                        Text(agency)
                        Text("·")
                        Text(distance)
                        Text("·")
                        Text(travelTime)
                        Text("·")
                        Text(price)
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.width(52.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(vacantNow, style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp), maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.height(12.dp)) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        Icon(
                            painter = painterResource(id = R.drawable.predicted),
                            contentDescription = "Predicted vacancy",
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(vacantPredicted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

        }
    }
}

@Composable
fun CarparkInfo(carPark: CarparkAvailability, modifier: Modifier = Modifier) {
    if (carPark.info == null) return
    val mapViewModel = viewModel<MapViewModel>()
    val historyViewModel = viewModel<HistoryViewModel>()
    val lots = carPark.info.getApplicableLots(VehicleType.CAR, isHoliday = false)

    // Map Name and Back Button
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CarparkBackButton(mapViewModel = mapViewModel)
        CarparkMinimap(carpark = carPark)
        Column(
            // verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .width(350.dp)
        ) {
            CarparkDetails(carpark = carPark, lots = lots, modifier = modifier)
            Spacer(modifier = Modifier.height(10.dp))
            // Lot Box
            Row(
                // horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            )
            {
                CarparkDistanceBox(carPark, modifier = modifier)
                Spacer(modifier = Modifier.width(20.dp))
                CarparkLotBox(carPark, VehicleType.CAR, modifier = modifier)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    historyViewModel.updateHistory(
                        StoredLocation(
                            tag = carPark.info.name,
                            name = carPark.info.name,
                            address = carPark.info.address,
                            epsg4326 = carPark.info.epsg4326
                        )
                    )
                    mapViewModel.redirectGoogleMaps()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Get Directions",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.primary),
                    fontSize = 18.sp,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CarparkMinimap(carpark: CarparkAvailability, modifier: Modifier = Modifier) {
    carpark.info ?: return
    Box(
        modifier = Modifier
            .height(200.dp)
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .width(350.dp)
                .align(Alignment.Center)
                .clip(shape = RoundedCornerShape(20.dp))
                .border(
                    shape = RoundedCornerShape(20.dp),
                    width = 1.dp,
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.outline)
                )
        )
        {
            MapboxV2(
                camera = carpark.info.epsg4326,
                visibleCarparks = listOf(carpark),
                enabled = false, // disable scrolling map
                animateCamera = false
            )
        }
    }
}

@Composable
fun CarparkInfoBar(carPark: CarparkAvailability, modifier: Modifier = Modifier) {
    if (carPark.info == null) return
    val mapViewModel = viewModel<MapViewModel>()
    val distance = carPark.distance?.div(1000) ?: 0
    // Get the three types of lots
    val lots = carPark.info.getApplicableLots(VehicleType.CAR, carPark.asof, carPark.asof.isHoliday())
    val price = lots.minPriceOrNull()?.let { currencyFormatter.format(it.rate.toDouble() / 100) } ?: "N/A"
    val carLots = carPark.lots[VehicleType.CAR] ?: CarparkAvailability.Inner(
        -1, -1
    )
    val heavyVehLots = carPark.lots[VehicleType.HEAVY_VEHICLE] ?: CarparkAvailability.Inner(
        -1, -1
    )
    val motorCycleLots = carPark.lots[VehicleType.MOTORCYCLE] ?: CarparkAvailability.Inner(
        -1, -1
    )
    // The type of lot based on user
    val lotType = carLots

    // if there exists lots in this carpark (not HDB)
    val carparkLot = if (carPark.info.lots.isNotEmpty()) {
        carPark.info.lots
    } else {
        null
    }


    Box(modifier.clickable {
        mapViewModel.moveMapCamera(carPark.info.epsg4326)
        mapViewModel.display(carPark)
        mapViewModel.toggleBottomSheet()
    }) {
        Column(
            modifier
                .border(
                    0.5.dp,
                    contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = carPark.info.name,
                modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
            Row(
                modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Distance")
                    Text(text = "${distance.toString()} km")
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Price")
                    // TODO : Change based on the current day
                    Text(text = price)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Lots Available")
                    // TODO : Change based on user vehicle type (lot type is altered above)
                    Text(
                        text = if (lotType.current < 0) "-"
                        else lotType.current.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun filterAndSort(
    availableCarparks: List<CarparkAvailability>,
    filteredList: MutableState<List<CarparkAvailability>>,
    modifier: Modifier = Modifier
) {
    var allButtonsPressed: Boolean = false
    var distanceButton by remember { mutableStateOf<Boolean?>(null) }
    var priceButton by remember { mutableStateOf<Boolean?>(null) }
    var lotsButton by remember { mutableStateOf<Boolean?>(null) }
    var paymentButton by remember { mutableStateOf<Boolean?>(null) }
    var vehicleButton by remember { mutableStateOf<Boolean?>(null) }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                Text(text = "Filter and Sort By:")
            }

            item {
                Button(
                    onClick = {
                        distanceButton = when (distanceButton) {
                            true -> false
                            false -> true
                            else -> true
                        }

                        val distance_filter = DistanceFilter(500.0, 10.0)
                        val newFilteredList = distance_filter.apply(availableCarparks)

                        if (distanceButton == true) filteredList.value =
                            newFilteredList.sortedBy { it.distance }
                        else if (distanceButton == false) filteredList.value =
                            newFilteredList.sortedByDescending { it.distance }

                    },
                ) {
                    Text(text = "Distance")
                }
            }

            item {
                Button(
                    onClick = {
                        priceButton = when (priceButton) {
                            true -> false
                            false -> true
                            else -> true
                        }

                        val price_filter = PriceFilter(1000.0, 0.0)
                        val newFilteredList = price_filter.apply(availableCarparks)

                        if (priceButton == true) filteredList.value =
                            newFilteredList.sortedBy { availability ->
                                val carpark = availability.info
                                carpark?.lots?.firstOrNull()?.rate?.toDouble() ?: Double.MIN_VALUE
                            }
                        else if (priceButton == false) {
                            filteredList.value =
                                newFilteredList.sortedByDescending { availability ->
                                    val carpark = availability.info
                                    carpark?.lots?.firstOrNull()?.rate?.toDouble()
                                        ?: Double.MIN_VALUE
                                }
                        }

                    },
                ) {
                    Text(text = "Price")
                }
            }

            item {
                Button(
                    onClick = {
                        lotsButton = when (lotsButton) {
                            true -> false
                            false -> true
                            else -> true
                        }

                        val lots_filter = MinimumLotsFilter(VehicleType.CAR, 5)
                        val newFilteredList = lots_filter.apply(availableCarparks)

                        if (lotsButton == true) filteredList.value =
                            newFilteredList.sortedBy { it.lots[VehicleType.CAR]?.current }
                        else if (lotsButton == false) filteredList.value =
                            newFilteredList.sortedByDescending { it.lots[VehicleType.CAR]?.current }

                    },
                ) {
                    Text(text = "Lots")
                }
            }

            item {
                Button(
                    onClick = {

                        val payment_filter = PaymentModeFilter(ParkingSystem.ELECTRONIC)
                        val newFilteredList = payment_filter.apply(availableCarparks)

                        filteredList.value =
                            newFilteredList.sortedBy { it.info?.lots?.getOrNull(0)?.system }
                    },
                ) {
                    Text(text = "Payment Mode")
                }
            }

            item {
                Button(
                    onClick = {
                        val vehicle_filter = VehicleTypeFilter(VehicleType.CAR)
                        val newFilteredList = vehicle_filter.apply(availableCarparks)

                        filteredList.value =
                            newFilteredList.sortedBy { it.lots[vehicle_filter.getAccept()]?.current }

                    },
                ) {
                    Text(text = "Vehicle Type")
                }
            }
        }

        allButtonsPressed =
            (distanceButton != null) || (priceButton != null) || (lotsButton != null) || (paymentButton != null) || (vehicleButton != null)

        if (allButtonsPressed) {
            if (distanceButton == true || priceButton == true || lotsButton == true || (paymentButton == true) || (vehicleButton == true)) {
                Text(text = "Ascending order", fontSize = 10.sp, fontWeight = FontWeight.Thin)
            } else {
                Text(text = "Descending order", fontSize = 10.sp, fontWeight = FontWeight.Thin)
            }
        } else {
            Text(
                text = "Tap to sort in ascending or descending order.",
                fontSize = 10.sp,
                fontWeight = FontWeight.Thin
            )
        }
    }
}

@Composable
fun createLazyColumn(availableCaparks: List<CarparkAvailability>) {
    LazyColumn() {
        items(items = availableCaparks, itemContent = { item ->
            CarparkInfoBar(item)
        })
    }
}


@Composable
fun CarparkBackButton(
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .height(65.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
        ) {
            Icon(imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                modifier = modifier
                    .size(35.dp)
                    .clickable {
                        mapViewModel.hide()
                    })
            ClickableText(
                text = AnnotatedString("Back"),
                style = TextStyle(
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                    fontSize = 20.sp,

                    ),
                onClick = {
                    mapViewModel.hide()
                },
            )
        }
    }
}

@Composable
fun CarparkDetails(carpark: CarparkAvailability, lots: List<Lot>, modifier: Modifier) {
    carpark.info ?: return
    val address = when {
        carpark.info.address.isBlank() -> carpark.info.name
        else -> carpark.info.address
    }
    val price = lots.minPriceOrNull()?.let { currencyFormatter.format(it.rate.toDouble() / 100) } ?: "N/A"
    // Get the applicable lots to us
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = carpark.info?.name ?: "Unknown Carpark",
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
        textAlign = TextAlign.Center,
        modifier = modifier
            //.align(Alignment.CenterHorizontally)
            .fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = address,
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
        textAlign = TextAlign.Center,
        modifier = modifier
            //.align(Alignment.CenterHorizontally)
            .fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(

    )
    {
        Image(
            painter = painterResource(R.drawable.dollar),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .padding(end = 4.dp)
        )
        Text(
            text = "M-F: $price",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 6.dp)
        )
        Text(
            text = "Sat: $price",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 6.dp)
        )
        Text(
            text = "Sun/PH: $price",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 6.dp)
        )
    }
}

@Composable
fun CarparkDistanceBox(carpark: CarparkAvailability, modifier: Modifier) {
    Box(
        modifier
            .background(
                contentColorFor(backgroundColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(15.dp)
            )
            .height(120.dp)
            .width(165.dp)
            .border(
                shape = RoundedCornerShape(15.dp),
                width = 1.dp,
                color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.outline)
            )
    ) {
        Column(
            modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.motorway),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    text = "Travelling",
                    fontWeight = FontWeight.Medium,
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Row(
                modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time: ",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                Text(
                    text = "${carpark.travelTime} mins",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                // Assign our text accordingly

            }
            Row(
                modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Distance: ",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                val distance = String.format("%.1f", carpark.distance?.toFloat()?.div(1000))
                Text(
                    text = "${distance} km",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

@Composable
fun CarparkLotBox(carpark: CarparkAvailability, userType: VehicleType, modifier: Modifier) {
    val lot = carpark.lots.getOrDefault(userType, CarparkAvailability.Inner(-1, -1))
    val currentLotText = if (lot.current == -1) "-" else lot.current
    val predictedLotText = if (lot.predicted == -1) "-" else lot.predicted

    Box(
        modifier
            .background(
                contentColorFor(backgroundColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(15.dp)
            )
            .height(120.dp)
            .width(165.dp)
            .border(
                shape = RoundedCornerShape(15.dp),
                width = 1.dp,
                color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.outline)
            )
    ) {
        Column(
            modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.parking_area),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 30.dp)
                        .size(50.dp)

                )
                Text(
                    text = "Lots",
                    fontWeight = FontWeight.Medium,
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp)
                )
            }
            Row(
                modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: ",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                Text(
                    text = "${currentLotText} lots",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                // Assign our text accordingly

            }
            Row(
                modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (lot.predicted == -1) "Predicted*: " else "Predicted: ",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
                Text(
                    text = "${predictedLotText} lots",
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}


/*
@Preview(showBackground = true)
@Composable
fun Preview() {
    //CarparkInfo()
}
*/

