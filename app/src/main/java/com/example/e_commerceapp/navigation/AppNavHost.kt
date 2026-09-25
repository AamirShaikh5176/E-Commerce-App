package com.example.e_commerceapp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.e_commerceapp.ui.screens.auth.SignInScreen
import com.example.e_commerceapp.ui.screens.auth.SignUpScreen
import com.example.e_commerceapp.ui.screens.cart.CartScreen
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.details.ProductDetailScreen
import com.example.e_commerceapp.ui.screens.favorite.FavoriteScreen
import com.example.e_commerceapp.ui.screens.home.HomeScreen
import com.example.e_commerceapp.ui.screens.payment.PaymentWebViewScreen
import com.example.e_commerceapp.ui.screens.payment.paymentResult.PaymentResultScreen
import com.example.e_commerceapp.ui.screens.payment.paymetMethod.PaymentMethod
import com.example.e_commerceapp.ui.screens.payment.paymetMethod.PaymentMethodScreen
import com.example.e_commerceapp.ui.screens.profile.ProfileScreen
import com.example.e_commerceapp.ui.screens.serach.SearchScreen
import com.example.e_commerceapp.ui.screens.splash.SplashScreen
import com.example.e_commerceapp.ui.screens.address.AddEditAddressScreen
import com.example.e_commerceapp.ui.screens.address.AddressSelectionScreen
import com.example.e_commerceapp.ui.screens.admin.AddEditProductScreen
import com.example.e_commerceapp.ui.screens.admin.AdminCategoryScreen
import com.example.e_commerceapp.ui.screens.admin.AdminDashboardScreen
import com.example.e_commerceapp.ui.screens.admin.AdminOrderListScreen
import com.example.e_commerceapp.ui.screens.admin.AdminProductListScreen
import com.example.e_commerceapp.ui.screens.admin.AdminUserListScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = SplashScreen.route,
        modifier = modifier
    ) {

        composable(SplashScreen.route) {
            HideNavigationBarOnly()
            SplashScreen(
                navigateToNextScreen = { rout ->
                    rout.let {
                        navController.navigate(it) {
                            popUpTo(SplashScreen.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(SignUpScreen.route) {
            SignUpScreen(
                navigateToHomeScreen = { userInfirmationEntity ->
                    navController.navigate(HomeScreen.route)
                },
                navigateToSignInScreen = {
                    navController.navigate(SignInScreen.route)
                },
            )
        }

        composable(SignInScreen.route) {
            SignInScreen(
                navigateToSignUpScreen = {
                    navController.navigate(SignUpScreen.route)
                },
                navigateToHomeScreen = {
                    navController.navigate(HomeScreen.route)
                },
            )
        }

        composable(HomeScreen.route) {
            HomeScreen(
                onProductClicked = {
                    val route = "${ProductDetailsScreen.route}/${it.id}"
                    Log.i("TAG_route", "AppNavHost: $route")
                    navController.navigate(route = route)
                },
                onClickSearchBar = {
                    navController.navigate(SearchScreen.route)
                },
                onNotificationClick = {
//                    navController.navigate(NotificationScreen.route)
                },
                onSeeAllCategoryClick = {
//                    navController.navigate(SeeAllCategoryScreen.route)
                }
            )
        }

        composable(
            route = "${ProductDetailsScreen.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            ProductDetailScreen(
                productId = productId,
                onBuyNowClick = {
                    val route = "${AddressSelectionDestination.route}/${it.price}"
                    navController.navigate(route = route)
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(CartScreen.route) {
            CartScreen(
                onCheckoutClicked = { totalPrice ->
                    val route = "${AddressSelectionDestination.route}/${totalPrice}"
                    navController.navigate(route = route)
                },
                onCartClicked = {
                    val route = "${ProductDetailsScreen.route}/${it.productId}"
                    navController.navigate(route = route)
                },
                onBackClick = { navController.popBackStack() },
                onBadgeCountChange = {},
            )
        }

        composable(FavoriteScreen.route) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = {
//                    navController.navigate(NotificationScreen.route)
                },
                onClickedFavoriteProduct = {
                    val route = "${ProductDetailsScreen.route}/${it.productId}"
                    navController.navigate(route = route)
                }
            )
        }

        composable(ProfileScreen.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                logout = {
                    navController.navigate(SignInScreen.route) {
                        popUpTo(SignInScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onSettings = {},
                onClickedChangeTheme = {},
                onClickedChangePass = {},
                onNavigateToAddresses = {
                    navController.navigate("${AddressSelectionDestination.route}/0.0")
                },
                onNavigateToAdmin = {
                    navController.navigate(AdminDashboardDestination.route)
                }
            )
        }

        composable(SearchScreen.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onProductClicked = {
                    val route = "${ProductDetailsScreen.route}/${it.id}"
                    navController.navigate(route = route)
                }
            )
        }

        // ------------------------------------------- Payment ----------------------------------------

        composable(
            route = "${PaymentMethodScreen.route}/{totalPrice}",
            arguments = listOf(navArgument("totalPrice") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalPrice = backStackEntry.arguments?.getString("totalPrice") ?: "0.0"

            PaymentMethodScreen(
                totalPrice = totalPrice,
                onBackClick = { navController.popBackStack() },
                onPaymentCompleted = { orderId, success ->
                    val route = "${PaymentResultScreen.route}/$success/$orderId"
                    navController.navigate(route = route) {
                        popUpTo(PaymentMethodScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = "${PaymentResultScreen.route}/{paymentSuccess}/{orderId}",
            arguments = listOf(
                navArgument("paymentSuccess") { type = NavType.BoolType },
                navArgument("orderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val paymentSuccess = backStackEntry.arguments?.getBoolean("paymentSuccess") ?: false
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

            PaymentResultScreen(
                paymentSuccess = paymentSuccess,
                orderId = orderId,
                onButtonClicked = {
                    if (paymentSuccess) {
                        navController.navigate(HomeScreen.route) {
                            popUpTo(0)
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = "${PaymentResultScreen.route}/{paymentSuccess}",
            arguments = listOf(navArgument("paymentSuccess") { type = NavType.BoolType })
        ) { backStackEntry ->
            val paymentSuccess = backStackEntry.arguments?.getBoolean("paymentSuccess") ?: false

            PaymentResultScreen(
                paymentSuccess = paymentSuccess,
                orderId = "",
                onButtonClicked = {
                    if (paymentSuccess) {
                        navController.navigate(HomeScreen.route) {
                            popUpTo(0)
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        // --------------------- Address Routes ---------------------
        composable(
            route = "${AddressSelectionDestination.route}/{totalPrice}",
            arguments = listOf(navArgument("totalPrice") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalPrice = backStackEntry.arguments?.getString("totalPrice") ?: "0.0"

            AddressSelectionScreen(
                totalPrice = totalPrice,
                onBackClick = { navController.popBackStack() },
                onAddNewAddress = { navController.navigate(AddEditAddressDestination.route) },
                onEditAddress = { addressId ->
                    navController.navigate("${AddEditAddressDestination.route}/$addressId")
                },
                onDeliverHere = { address ->
                    val route = "${PaymentMethodScreen.route}/$totalPrice"
                    navController.navigate(route = route)
                }
            )
        }

        composable(AddEditAddressDestination.route) {
            AddEditAddressScreen(
                addressId = null,
                onBackClick = { navController.popBackStack() },
                onAddressSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "${AddEditAddressDestination.route}/{addressId}",
            arguments = listOf(navArgument("addressId") { type = NavType.StringType })
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getString("addressId")
            AddEditAddressScreen(
                addressId = addressId,
                onBackClick = { navController.popBackStack() },
                onAddressSaved = { navController.popBackStack() }
            )
        }

        // --------------------- Admin Routes (Protected by AdminAuthGuard) ---------------------
        composable(AdminDashboardDestination.route) {
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AdminDashboardScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToProducts = { navController.navigate(AdminProductListDestination.route) },
                    onNavigateToCategories = { navController.navigate(AdminCategoryDestination.route) },
                    onNavigateToOrders = { navController.navigate(AdminOrderListDestination.route) },
                    onNavigateToUsers = { navController.navigate(AdminUserListDestination.route) },
                )
            }
        }

        composable(AdminProductListDestination.route) {
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AdminProductListScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddProduct = { navController.navigate(AddEditProductDestination.route) },
                    onEditProduct = { productId ->
                        navController.navigate("${AddEditProductDestination.route}/$productId")
                    }
                )
            }
        }

        composable(AddEditProductDestination.route) {
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AddEditProductScreen(
                    productId = null,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = "${AddEditProductDestination.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AddEditProductScreen(
                    productId = productId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(AdminCategoryDestination.route) {
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AdminCategoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(AdminOrderListDestination.route) {
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AdminOrderListScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(AdminUserListDestination.route) {
            AdminAuthGuard(onUnauthorized = { navController.popBackStack() }) {
                AdminUserListScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

    }
}
