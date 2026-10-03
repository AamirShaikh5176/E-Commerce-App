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
import com.example.e_commerceapp.ui.screens.home.CategoryProductsScreen
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
import com.example.e_commerceapp.ui.screens.admin.AdminLoginScreen
import com.example.e_commerceapp.ui.screens.admin.AdminOrderListScreen
import com.example.e_commerceapp.ui.screens.admin.AdminProductListScreen
import com.example.e_commerceapp.ui.screens.admin.AdminUserListScreen
import com.example.e_commerceapp.ui.screens.orders.MyOrdersScreen
import com.example.e_commerceapp.ui.screens.orders.OrderDetailsScreen
import com.example.e_commerceapp.ui.screens.settings.SettingsScreen
import com.example.e_commerceapp.ui.screens.support.HelpSupportScreen
import com.example.e_commerceapp.ui.screens.supercoins.SuperCoinsScreen
import com.example.e_commerceapp.ui.screens.wallet.WalletScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.e_commerceapp.utils.AuthUtils
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    var pendingCheckoutTotalPrice by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

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
                    val pendingPrice = pendingCheckoutTotalPrice
                    android.util.Log.d(
                        "BuyNowAuth",
                        "SignUpScreen success: pendingCheckoutTotalPrice = $pendingPrice"
                    )
                    if (pendingPrice != null) {
                        pendingCheckoutTotalPrice = null
                        val route = "${AddressSelectionDestination.route}/$pendingPrice"
                        android.util.Log.d("BuyNowAuth", "Continuing to address selection: $route")
                        navController.navigate(route) {
                            popUpTo(ProductDetailsScreen.route + "/{productId}") { inclusive = false }
                        }
                    } else {
                        navController.navigate(HomeScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                navigateToSignInScreen = {
                    navController.navigate(SignInScreen.route) {
                        popUpTo(SignUpScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(SignInScreen.route) {
            SignInScreen(
                navigateToSignUpScreen = {
                    navController.navigate(SignUpScreen.route)
                },
                navigateToAdminLogin = {
                    navController.navigate(AdminLoginDestination.route) {
                        popUpTo(SignInScreen.route) { inclusive = true }
                    }
                },
                navigateToHomeScreen = {
                    val pendingPrice = pendingCheckoutTotalPrice
                    android.util.Log.d(
                        "BuyNowAuth",
                        "SignInScreen success: pendingCheckoutTotalPrice = $pendingPrice"
                    )
                    if (pendingPrice != null) {
                        pendingCheckoutTotalPrice = null
                        val route = "${AddressSelectionDestination.route}/$pendingPrice"
                        android.util.Log.d("BuyNowAuth", "Continuing to address selection: $route")
                        navController.navigate(route) {
                            popUpTo(ProductDetailsScreen.route + "/{productId}") { inclusive = false }
                        }
                    } else {
                        navController.navigate(HomeScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
                onCategoryClick = { categoryName ->
                    navController.navigate("${CategoryProductsDestination.route}/$categoryName")
                },
                onSeeAllCategoryClick = { title ->
                    navController.navigate("${CategoryProductsDestination.route}/$title")
                }
            )
        }

        composable(
            route = "${CategoryProductsDestination.route}/{categoryTitle}",
            arguments = listOf(navArgument("categoryTitle") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryTitle = backStackEntry.arguments?.getString("categoryTitle") ?: "Products"
            CategoryProductsScreen(
                categoryTitle = categoryTitle,
                onProductClicked = { product ->
                    val route = "${ProductDetailsScreen.route}/${product.id}"
                    navController.navigate(route = route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "${ProductDetailsScreen.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            ProductDetailScreen(
                productId = productId,
                onBuyNowClick = { product ->
                    val effectivePrice = (product.price * product.quantity).toString()
                    val route = "${AddressSelectionDestination.route}/$effectivePrice"
                    val isAuthed = AuthUtils.isAuthenticated(context)
                    android.util.Log.d(
                        "BuyNowAuth",
                        "AppNavHost onBuyNowClick: isAuthed -> $isAuthed, route -> $route"
                    )
                    if (!isAuthed) {
                        pendingCheckoutTotalPrice = effectivePrice
                        navController.navigate(SignInScreen.route)
                    } else {
                        navController.navigate(route = route)
                    }
                },
                onLoginClick = { product ->
                    val effectivePrice = (product.price * product.quantity).toString()
                    pendingCheckoutTotalPrice = effectivePrice
                    android.util.Log.d(
                        "BuyNowAuth",
                        "AppNavHost onLoginClick: pendingPrice=$effectivePrice, navigating to SignIn"
                    )
                    navController.navigate(SignInScreen.route)
                },
                onCreateAccountClick = { product ->
                    val effectivePrice = (product.price * product.quantity).toString()
                    pendingCheckoutTotalPrice = effectivePrice
                    android.util.Log.d(
                        "BuyNowAuth",
                        "AppNavHost onCreateAccountClick: pendingPrice=$effectivePrice, navigating to SignUp"
                    )
                    navController.navigate(SignUpScreen.route)
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(CartScreen.route) {
            CartScreen(
                onCheckoutClicked = { totalPrice ->
                    val route = "${AddressSelectionDestination.route}/${totalPrice}"
                    val isAuthed = AuthUtils.isAuthenticated(context)
                    android.util.Log.d(
                        "BuyNowAuth",
                        "AppNavHost Cart onCheckoutClicked: isAuthed -> $isAuthed, route -> $route"
                    )
                    if (!isAuthed) {
                        pendingCheckoutTotalPrice = totalPrice
                        navController.navigate(SignInScreen.route)
                    } else {
                        navController.navigate(route = route)
                    }
                },
                onCartClicked = {
                    val route = "${ProductDetailsScreen.route}/${it.productId}"
                    navController.navigate(route = route)
                },
                onLoginClick = { totalPrice ->
                    pendingCheckoutTotalPrice = totalPrice
                    android.util.Log.d(
                        "BuyNowAuth",
                        "AppNavHost Cart onLoginClick: pendingPrice=$totalPrice, navigating to SignIn"
                    )
                    navController.navigate(SignInScreen.route)
                },
                onCreateAccountClick = { totalPrice ->
                    pendingCheckoutTotalPrice = totalPrice
                    android.util.Log.d(
                        "BuyNowAuth",
                        "AppNavHost Cart onCreateAccountClick: pendingPrice=$totalPrice, navigating to SignUp"
                    )
                    navController.navigate(SignUpScreen.route)
                },
                onBackClick = { navController.popBackStack() },
                onStartShoppingClick = {
                    navController.navigate(HomeScreen.route) {
                        popUpTo(HomeScreen.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onBadgeCountChange = {},
            )
        }

        composable(FavoriteScreen.route) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onContinueShoppingClick = {
                    navController.navigate(HomeScreen.route) {
                        popUpTo(HomeScreen.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
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
                    AuthUtils.logout(context)
                    navController.navigate(HomeScreen.route) {
                        popUpTo(0)
                    }
                },
                onSettings = {
                    navController.navigate(SettingsDestination.route)
                },
                onClickedChangeTheme = {},
                onClickedChangePass = {},
                onNavigateToAddresses = {
                    if (!AuthUtils.isAuthenticated(context)) {
                        navController.navigate(SignInScreen.route)
                    } else {
                        navController.navigate(ManageAddressesDestination.route)
                    }
                },
                onNavigateToMyOrders = {
                    if (!AuthUtils.isAuthenticated(context)) {
                        navController.navigate(SignInScreen.route)
                    } else {
                        navController.navigate(MyOrdersDestination.route)
                    }
                },
                onNavigateToWishlist = {
                    navController.navigate(FavoriteScreen.route)
                },
                onNavigateToHelpSupport = {
                    navController.navigate(HelpSupportDestination.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(AdminDashboardDestination.route)
                },
                onNavigateToAdminLogin = {
                    navController.navigate(AdminLoginDestination.route)
                },
                onNavigateToLogin = {
                    navController.navigate(SignInScreen.route)
                },
                onNavigateToSuperCoins = {
                    navController.navigate(SuperCoinsDestination.route)
                },
                onNavigateToWallet = {
                    if (!AuthUtils.isAuthenticated(context)) {
                        navController.navigate(SignInScreen.route)
                    } else {
                        navController.navigate(WalletDestination.route)
                    }
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
                },
                onViewOrdersClicked = {
                    navController.navigate(MyOrdersDestination.route) {
                        popUpTo(HomeScreen.route) { inclusive = false }
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
                },
                onViewOrdersClicked = {
                    navController.navigate(MyOrdersDestination.route) {
                        popUpTo(HomeScreen.route) { inclusive = false }
                    }
                }
            )
        }

        // --------------------- Address Routes ---------------------
        composable(ManageAddressesDestination.route) {
            AddressSelectionScreen(
                totalPrice = "0.0",
                isCheckoutMode = false,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(SignInScreen.route) },
                onAddNewAddress = { navController.navigate(AddEditAddressDestination.route) },
                onEditAddress = { addressId ->
                    navController.navigate("${AddEditAddressDestination.route}/$addressId")
                },
                onDeliverHere = {}
            )
        }

        composable(
            route = "${AddressSelectionDestination.route}/{totalPrice}",
            arguments = listOf(navArgument("totalPrice") { type = NavType.StringType })
        ) { backStackEntry ->
            val totalPrice = backStackEntry.arguments?.getString("totalPrice") ?: "0.0"

            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            androidx.compose.runtime.LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    try {
                        val tokenResult = currentUser.getIdToken(false).await()
                        if (tokenResult.claims["admin"] == true) {
                            android.widget.Toast.makeText(context, "Admin accounts cannot place customer orders.", android.widget.Toast.LENGTH_SHORT).show()
                            navController.navigate(AdminDashboardDestination.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }

            AddressSelectionScreen(
                totalPrice = totalPrice,
                isCheckoutMode = true,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(SignInScreen.route) },
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
                    onLogout = {
                        AuthUtils.logout(context)
                        navController.navigate(AdminLoginDestination.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
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

        // --------------------- Settings & Orders Routes ---------------------
        composable(SettingsDestination.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onChangePasswordClick = {}
            )
        }

        composable(HelpSupportDestination.route) {
            HelpSupportScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToOrderDetails = { orderId ->
                    navController.navigate("${OrderDetailsDestination.route}/$orderId")
                },
                onNavigateToLogin = {
                    navController.navigate(SignInScreen.route)
                }
            )
        }

        composable(SuperCoinsDestination.route) {
            SuperCoinsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(WalletDestination.route) {
            WalletScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(MyOrdersDestination.route) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                navController.navigate(SignInScreen.route)
            } else {
                MyOrdersScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderClick = { orderId ->
                        navController.navigate("${OrderDetailsDestination.route}/$orderId")
                    },
                    onContinueShopping = {
                        navController.navigate(HomeScreen.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        composable(
            route = "${OrderDetailsDestination.route}/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailsScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AdminLoginDestination.route) {
            AdminLoginScreen(
                onBackClick = { navController.popBackStack() },
                onAdminLoginSuccess = {
                    navController.navigate(AdminDashboardDestination.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCustomerLoginClick = {
                    navController.navigate(SignInScreen.route)
                }
            )
        }

    }
}
