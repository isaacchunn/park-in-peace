package ntu26.ss.parkinpeace.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ntu26.ss.parkinpeace.android.theme.MyApplicationTheme
import ntu26.ss.parkinpeace.android.utils.LocalNavHostController
import ntu26.ss.parkinpeace.android.utils.NavItem
import ntu26.ss.parkinpeace.android.utils.Route
import ntu26.ss.parkinpeace.android.utils.allRoutes
import ntu26.ss.parkinpeace.android.views.HistoryUI
import ntu26.ss.parkinpeace.android.views.MapUI
import ntu26.ss.parkinpeace.android.views.SettingsUI
import ntu26.ss.parkinpeace.models.Coordinate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CompositionLocalProvider(LocalNavHostController provides navController) {
                        Mount()
                    }
                }
            }
        }
    }
}

@Composable
fun Root(modifier: Modifier = Modifier, host: NavHostController) {
    val vmso = LocalViewModelStoreOwner.current!!
    NavHost(
        navController = host,
        startDestination = Route.Explore().route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }) {
        composable(Route.History.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { // Override navigation store owner
                HistoryUI()
            }
        }
        composable(Route.Explore().route, arguments = listOf(navArgument("nearby") { nullable = true })) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { // Override navigation store owner
                MapUI(nearby = Coordinate.parseOrNull(it.arguments?.getString("nearby")))
            }
        }
        composable(Route.Settings.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides vmso) { // Override navigation store owner
                SettingsUI()
            }
        }
    }
}

@Composable
fun Mount() {
    val host = LocalNavHostController.current
    val items = allRoutes
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by host.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item: NavItem ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true
                    val title = stringResource(item.route.resourceId)
                    val icon = if (selected) item.selectedIcon else item.unselectedIcon
                    NavigationBarItem(
                        icon = { Icon(imageVector = icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selected,
                        onClick = {
                            host.navigate(item.route.baseRoute) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(host.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Root(modifier = Modifier.padding(innerPadding), host = host)
    }
}
