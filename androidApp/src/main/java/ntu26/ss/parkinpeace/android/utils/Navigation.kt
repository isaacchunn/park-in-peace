package ntu26.ss.parkinpeace.android.utils

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import ntu26.ss.parkinpeace.android.R
import ntu26.ss.parkinpeace.models.Coordinate

val LocalNavHostController = compositionLocalOf<NavHostController> { error("No NavHostController found!") }

sealed class Route(val route: String, val baseRoute: String, @StringRes val resourceId: Int) {
    data object History : Route("history", "history", R.string.title_history)
    data class Explore(val nearby: Coordinate? = null) :
        Route("explore?nearby={nearby}", "explore", R.string.title_explore)

    data object Settings : Route("settings", "settings", R.string.title_settings)
}

fun Route.asPath(): String {
    return when (this) {
        is Route.History, is Route.Settings -> route
        is Route.Explore -> when (nearby) {
            null -> baseRoute
            else -> listOf(baseRoute, "?nearby=${nearby.urlSafe}").joinToString("")
        }
    }
}

data class NavItem(
    val route: Route,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasBadge: Boolean,
    val badgeNum: Int
)

val allRoutes = listOf(
    NavItem(
        route = Route.History,
        selectedIcon = Icons.Filled.Refresh,
        unselectedIcon = Icons.Outlined.Refresh,
        hasBadge = false,
        badgeNum = 0
    ), NavItem(
        route = Route.Explore(),
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place,
        hasBadge = false,
        badgeNum = 0
    ), NavItem(
        route = Route.Settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        hasBadge = false,
        badgeNum = 0
    )
)

fun NavHostController.navigate(route: Route) = navigate(route.asPath()) {
    // Pop up to the start destination of the graph to
    // avoid building up a large stack of destinations
    // on the back stack as users select items
    // popUpTo(graph.findStartDestination().id) {
    //     saveState = true
    // }

    // Avoid multiple copies of the same destination when
    // reselecting the same item
    launchSingleTop = true

    // Restore state when reselecting a previously selected item
    // restoreState = true
}