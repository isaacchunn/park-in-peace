package ntu26.ss.parkinpeace.android.views

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.maps.AnnotatedFeature
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.attribution.generated.AttributionSettings
import com.mapbox.maps.plugin.compass.generated.CompassSettings
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.plugin.logo.generated.LogoSettings
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettings
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.delay
import ntu26.ss.parkinpeace.android.R
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.android.models.getApplicableLots
import ntu26.ss.parkinpeace.android.utils.toPoint
import ntu26.ss.parkinpeace.android.viewmodels.MapUIState
import ntu26.ss.parkinpeace.android.viewmodels.MapViewModel
import ntu26.ss.parkinpeace.android.viewmodels.PRESET_FILTERS
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType

@Composable
fun MapUI(modifier: Modifier = Modifier, nearby: Coordinate?) {
    val mapViewModel = viewModel<MapViewModel>()

    val prompt by mapViewModel.promptState.collectAsState()
    val show by mapViewModel.displayState.collectAsState()
    val isRecentering by mapViewModel.isRecentering.collectAsState()
    val mapUIState by mapViewModel.mapUIState.collectAsState()
    val carparks by mapViewModel.nearbyCarparks.collectAsState()
    val isSheetOpen by mapViewModel.btmSheetState.collectAsState()
    val sortingStrategy by mapViewModel.sortingStrategy.collectAsState()
    val vehicleType by mapViewModel.vehicle.collectAsState()
    val finalCarparks by mapViewModel.finalCarparks.collectAsState(carparks)
    val filters by mapViewModel.filters.collectAsState(PRESET_FILTERS)

    LaunchedEffect(mapViewModel) {
        mapViewModel.init()
    }

    LaunchedEffect(nearby) {
        println("nearby=$nearby")
        if (nearby != null && mapViewModel.coordinate.value != nearby) {
            delay(1000)
            mapViewModel.gotoLocation(nearby)
        }
    }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MainMap(mapViewModel)

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        ) {
            MapControls(isRecentering, onRecenter = mapViewModel::recenter) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { if (carparks.isNotEmpty()) mapViewModel.toggleBottomSheet() }) {
                    val text = when (carparks.size) {
                        0 -> "No car parks nearby"
                        else -> "View ${carparks.size} car parks nearby"
                    }
                    Text(text)
                }
            }
        }

        AnimatedVisibility(
            visible = prompt,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutHorizontally()
        ) { CarparkInfoPrompt(VehicleType.CAR) }

        if (show) CarparkInfoPopup()

        when (mapUIState) {
            is MapUIState.Loading -> {
                LoadingScreen(modifier = modifier.fillMaxSize())
            }

            is MapUIState.Success -> CarparkBottomSheet(
                modifier = Modifier.fillMaxWidth(),
                availableCarparks = finalCarparks,
                vehicleType = vehicleType,
                sortingStrategy = sortingStrategy,
                filters = filters,
                isOpen = isSheetOpen,
                onDismissRequest = mapViewModel::toggleBottomSheet,
                onSortChanged = mapViewModel::onSortStrategyChanged,
                onCarparkSelected = { cp ->
                    mapViewModel.moveMapCamera(cp.info!!.epsg4326)
                    mapViewModel.display(cp)
                    mapViewModel.toggleBottomSheet()
                },
                onFilterChanged = mapViewModel::onFilterChanged
            )

            is MapUIState.Error -> ErrorScreen(modifier = modifier.fillMaxSize())
        }
        SearchUI()
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun MapboxV2(
    modifier: Modifier = Modifier,
    camera: Coordinate,
    currentLocation: Coordinate? = null,
    destination: Coordinate? = null,
    visibleCarparks: List<CarparkAvailability> = listOf(),
    onCarparkSelected: (CarparkAvailability) -> Unit = {},
    onRenderCompleted: () -> Unit = {},
    mapboxLogo: Boolean = true,
    offsetAttribution: Boolean = false,
    animateCamera: Boolean = true,
    enabled: Boolean = true
) {
    val leftOffset = LocalDensity.current.run { 16.dp.toPx() }
    val bottomOffset = LocalDensity.current.run { 56.dp.toPx() }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(camera.toPoint())
            zoom(16.0)
        }
    }

    LaunchedEffect(camera) {
        mapViewportState.flyTo(
            cameraOptions = cameraOptions {
                center(camera.toPoint())
                zoom(16.0)
            },
            animationOptions = if (animateCamera) MapAnimationOptions.mapAnimationOptions {
                duration(
                    2000
                )
            } else null,
            completionListener = { if (it) onRenderCompleted() })
    }
    MapboxMap(
        modifier = modifier,
        mapInitOptionsFactory = { context ->
            MapInitOptions(
                context = context,
                styleUri = Style.TRAFFIC_DAY
            )
        },
        logoSettings = LogoSettings {
            this.enabled = mapboxLogo
            if (offsetAttribution) {
                marginBottom = bottomOffset; marginLeft = leftOffset
            }
        },
        compassSettings = CompassSettings { this.enabled = false },
        scaleBarSettings = ScaleBarSettings { this.enabled = false },
        attributionSettings = AttributionSettings { this.enabled = false },
        gesturesSettings = GesturesSettings { this.scrollEnabled = enabled },
        mapViewportState = mapViewportState
    ) {

        if (currentLocation != null) UserPin(currentLocation, onClick = {})

        if (destination != null) DestinationPin(destination, onClick = {})

        for (carpark in visibleCarparks) {
            carpark.info ?: continue
            CarparkPin(carpark.info.epsg4326, onClick = { onCarparkSelected(carpark) })
        }
    }
}

@Composable
fun UserPin(location: Coordinate, onClick: () -> Unit) {
    MapPin(
        location,
        painterResource(R.drawable.user_pin),
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primary,
        allowOverlap = true,
        onClick = onClick
    )
}

@Composable
fun DestinationPin(location: Coordinate, onClick: () -> Unit) {
    MapPin(
        location,
        painterResource(R.drawable.location_pin),
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.tertiary,
        allowOverlap = true,
        onClick = onClick
    )
}

@Composable
fun CarparkPin(location: Coordinate, onClick: () -> Unit) {
    MapPin(
        location,
        painterResource(R.drawable.carpark),
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.secondary,
        onClick = onClick
    )
}


@OptIn(MapboxExperimental::class)
@Composable
fun MapPin(
    coords: Coordinate,
    painter: Painter,
    tintDark: Color,
    tintLight: Color,
    allowOverlap: Boolean? = null,
    onClick: () -> Unit
) {
    ViewAnnotation(options = viewAnnotationOptions {
        annotatedFeature(AnnotatedFeature(coords.toPoint()))
        allowOverlap(allowOverlap)
    }) {
        Icon(
            painter = painter,
            tint = if (isSystemInDarkTheme()) tintDark else tintLight,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun MainMap(mapViewModel: MapViewModel) {
    val userCoords by mapViewModel.userCoordinate.collectAsState()
    val coordinate by mapViewModel.coordinate.collectAsState()
    val carparks by mapViewModel.nearbyCarparks.collectAsState()
    val mapCoords by mapViewModel.mapCoords.collectAsState()
    println("mainmap=$coordinate, mapCoordinates=$mapCoords")
    MapboxV2(
        camera = mapCoords,
        currentLocation = userCoords,
        destination = coordinate,
        visibleCarparks = carparks.toList(),
        onCarparkSelected = { mapViewModel.display(it) },
        onRenderCompleted = { mapViewModel.recentered() },
        offsetAttribution = true
    )
}

@Composable
fun RecenterButton(
    modifier: Modifier = Modifier,
    isRecentering: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition("recenter")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recenterAlpha"
    )

    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        if (isRecentering) {
            Icon(
                painter = painterResource(id = R.drawable.recenter),
                tint = LocalContentColor.current.copy(alpha = alpha),
                contentDescription = null
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.recenter),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarparkInfoPopup(modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState()
    val mapViewModel = viewModel<MapViewModel>()
    val carparkInfo by mapViewModel.displayCarpark.collectAsState()
    carparkInfo?.let { cp ->
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = mapViewModel::hide
        ) {
            CarparkInfo(carPark = cp)
        }
    }
}

@Composable
fun CarparkInfoPrompt(userType: VehicleType, modifier: Modifier = Modifier) {
    val mapViewModel = viewModel<MapViewModel>()
    val carparkInfo by mapViewModel.displayCarpark.collectAsState()
    carparkInfo?.let { cp ->
        val lots = cp.info?.getApplicableLots(VehicleType.CAR, isHoliday = false)
        val lotCapacity = cp.lots.getOrDefault(userType, CarparkAvailability.Inner(-1, -1))
        if (lots == null)
            return
        Box(
            modifier
                .fillMaxSize()
        ) {
            Box(
                modifier
                    .padding(bottom = 70.dp)
                    .align(Alignment.BottomCenter)
                    .height(140.dp)
                    .width(360.dp)
                    .clip(shape = RoundedCornerShape(50.dp))
                    .background(contentColorFor(backgroundColor = MaterialTheme.colorScheme.primary))
                    .clickable { mapViewModel.togglePopup() }
            ) {
                Column(
                    modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier
                            .padding(top = 10.dp)
                            .offset(x = (-30).dp)
                    ) {
                        Text(
                            text = cp.info.name.take(20) + "...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                        )
                        Text(
                            text = cp.info.address.take(30) + "...",
                            fontSize = 12.sp,
                            color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                    Row(
                        modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.parking_area),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(30.dp),
                                colorFilter = ColorFilter.tint(contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)),
                            )
                            Text(
                                text = "${lotCapacity.current} lots",
                                color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.padding(start = 3.dp, end = 10.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.motorway),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(30.dp),
                                colorFilter = ColorFilter.tint(contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)),
                            )
                            val distance = String.format("%.1f", cp.distance?.toFloat()?.div(1000))
                            Text(
                                text = "$distance km",
                                color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.padding(start = 3.dp, end = 10.dp)
                            )

                            if (lots.isNotEmpty()) {
                                Image(
                                    painter = painterResource(id = R.drawable.dollar),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(30.dp),
                                    colorFilter = ColorFilter.tint(contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)),
                                )
                                Text(
                                    text = "$0.${lots.getOrNull(0)?.rate ?: "00"}",
                                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.padding(start = 3.dp, end = 10.dp)
                                )
                            }
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
                    modifier = modifier
                        .size(35.dp)
                        .offset(x = 280.dp, y = 20.dp)
                        .clickable { mapViewModel.togglePrompt() }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val mapViewModel = viewModel<MapViewModel>()
    mapViewModel.hide()
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center,
        content = {
            Image(
                modifier = modifier.size(200.dp),
                painter = painterResource(R.drawable.loading_img),
                contentDescription = "Loading"
            )
        }
    )
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = null
        )
        Text(
            text = "Failed to load.\nDid you configure SECRETS.properties?",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun MapControls(
    isRecentering: Boolean,
    onRecenter: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Column {
        RecenterButton(
            modifier = Modifier.align(Alignment.End),
            onClick = onRecenter,
            isRecentering = isRecentering
        )
        Spacer(Modifier.height(8.dp))
        content(this)
    }
}