package com.example.e_commerceapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.e_commerceapp.R
import com.example.e_commerceapp.navigation.AppNavHost
import com.example.e_commerceapp.navigation.CartScreen
import com.example.e_commerceapp.navigation.FavoriteScreen
import com.example.e_commerceapp.navigation.HomeScreen
import com.example.e_commerceapp.navigation.ProfileScreen
import com.example.e_commerceapp.navigation.SignInScreen
import com.example.e_commerceapp.navigation.bottomNavigation.AppBottomNavBar
import com.example.e_commerceapp.navigation.bottomNavigation.BottomNavItem
import com.example.e_commerceapp.ui.theme.ECommerceAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ECommerceAppTheme {
                App()  // Start My App
            }
        }
    }
}


@Composable
fun App(
    modifier: Modifier = Modifier,
    appState: EcommerceAppState = rememberEcommerceAppState(),
) {
    val bottomNavigationItems = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Cart,
            BottomNavItem.Favorite,
            BottomNavItem.Profile,
        )
    }

    val navController = rememberNavController()
    val backStackState = navController.currentBackStackEntryAsState().value
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    selectedItem = when (backStackState?.destination?.route) {
        HomeScreen.route -> 0
        CartScreen.route -> 1
        FavoriteScreen.route -> 2
        ProfileScreen.route -> 3
        else -> 0
    }


    //Show bottom bar in this screens only
    val isBottomBarVisible = remember(key1 = backStackState) {
        backStackState?.destination?.route == HomeScreen.route ||
                backStackState?.destination?.route == CartScreen.route ||
                backStackState?.destination?.route == FavoriteScreen.route ||
                backStackState?.destination?.route == ProfileScreen.route
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {

        if (!appState.isOnline) {
            OfflineDialog(onRetry = appState::refreshOnline)
        } else {

            Scaffold(
                bottomBar = {
                    if (isBottomBarVisible) {
                        AppBottomNavBar(
                            items = bottomNavigationItems,
                            selectedItem = selectedItem,
                            onItemClick = { index ->
                                when (index) {
                                    0 -> navigateToTab(
                                        navController = navController,
                                        route = HomeScreen.route
                                    )

                                    1 -> navigateToTab(
                                        navController = navController,
                                        route = CartScreen.route
                                    )

                                    2 -> navigateToTab(
                                        navController = navController,
                                        route = FavoriteScreen.route
                                    )

                                    3 -> navigateToTab(
                                        navController = navController,
                                        route = ProfileScreen.route
                                    )
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

        }
    }
}


private fun navigateToTab(navController: NavController, route: String) {
    navController.navigate(route) {
        navController.graph.startDestinationRoute?.let { screen_route ->
            popUpTo(screen_route) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun rememberEcommerceAppState(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current,
) = remember(navController, context) {
    EcommerceAppState(navController, context)
}


class EcommerceAppState(
    val navController: NavHostController,
    private val context: Context,
) {
    var isOnline by mutableStateOf(checkIfOnline())
        private set

    fun refreshOnline() {
        isOnline = checkIfOnline()
    }

    fun navigateToSignIn(from: NavBackStackEntry) {
        // In order to discard duplicated navigation events, we check the Lifecycle
        if (from.lifecycleIsResumed()) {
            navController.navigate(SignInScreen.route)
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    private fun checkIfOnline(): Boolean {
        val cm = getSystemService(context, ConnectivityManager::class.java)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = cm?.getNetworkCapabilities(cm.activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            cm?.activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }
}

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED

@Composable
fun OfflineDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(R.string.app_name)) },
        text = { Text(text = stringResource(R.string.no_internet_connection_dialog)) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        },
    )
}
