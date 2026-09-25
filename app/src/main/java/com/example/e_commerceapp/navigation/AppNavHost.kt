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
                    val route = "${PaymentMethodScreen.route}/${it.price}"
                    navController.navigate(route = route)
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(CartScreen.route) {
            CartScreen(
                onCheckoutClicked = { totalPrice ->
                    val route = "${PaymentMethodScreen.route}/${totalPrice}"
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
                moveToPaymentWebView = { paymentKey, paymentMethod ->
                    val route = "${PaymentWebViewScreen.route}/${paymentKey}/${paymentMethod}"
                    navController.navigate(route = route)
                }
            )
        }

        composable(
            route = "${PaymentWebViewScreen.route}/{paymentKey}/{paymentMethod}",
            arguments = listOf(
                navArgument("paymentKey") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val paymentKey = backStackEntry.arguments?.getString("paymentKey") ?: ""
            val paymentMethod = backStackEntry.arguments?.getString("paymentMethod") ?: ""

            PaymentWebViewScreen(
                paymentKey = paymentKey,
                paymentMethod = PaymentMethod.valueOf(paymentMethod),
                moveToPaymentResultScreen = { paymentSuccess ->
                    val route = "${PaymentResultScreen.route}/${paymentSuccess}"
                    navController.navigate(route = route) {
                        popUpTo(PaymentMethodScreen.route) {
                            inclusive = false
                        }  // remove all top screens except cart screen
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
                onButtonClicked = {
                    if (paymentSuccess) {
                        navController.navigate(HomeScreen.route) {
                            popUpTo(0) // delete all back stack
                        }
                    } else {
                        // back to PaymentMethodScreen
                        navController.popBackStack(
                            route = PaymentMethodScreen.route,
                            inclusive = false
                        )
                    }
                }
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
