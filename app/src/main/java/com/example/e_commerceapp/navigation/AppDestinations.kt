package com.example.e_commerceapp.navigation

interface AppDestinations {
    val route: String
}


object ProductDetailsScreen : AppDestinations {
    override val route = "productDetail"
}

object SplashScreen : AppDestinations {
    override val route = "splash"
}

object SignInScreen : AppDestinations {
    override val route = "signIn"
}

object SignUpScreen : AppDestinations {
    override val route = "signUp"
}

object HomeScreen : AppDestinations {
    override val route = "home"
}

object CartScreen : AppDestinations {
    override val route = "cart"
}

object FavoriteScreen : AppDestinations {
    override val route = "favorite"
}

object ProfileScreen : AppDestinations {
    override val route = "profile"
}

object SearchScreen : AppDestinations {
    override val route = "search"
}

object PaymentMethodScreen : AppDestinations {
    override val route = "paymentMethod"
}

object PaymentWebViewScreen : AppDestinations {
    override val route = "paymentWebView"
}

object PaymentResultScreen : AppDestinations {
    override val route = "paymentResult"
}

// --------------------- Address Destinations ---------------------
object AddressSelectionDestination : AppDestinations {
    override val route = "address_selection"
}

object AddEditAddressDestination : AppDestinations {
    override val route = "add_edit_address"
}

// --------------------- Customer & Account Destinations ---------------------
object MyOrdersDestination : AppDestinations {
    override val route = "my_orders"
}

object OrderDetailsDestination : AppDestinations {
    override val route = "order_details"
}

object SettingsDestination : AppDestinations {
    override val route = "settings"
}

object HelpSupportDestination : AppDestinations {
    override val route = "help_support"
}

object SuperCoinsDestination : AppDestinations {
    override val route = "supercoins"
}

// --------------------- Admin Destinations ---------------------
object AdminLoginDestination : AppDestinations {
    override val route = "admin_login"
}

object AdminDashboardDestination : AppDestinations {
    override val route = "admin_dashboard"
}

object AdminProductListDestination : AppDestinations {
    override val route = "admin_products"
}

object AddEditProductDestination : AppDestinations {
    override val route = "admin_add_edit_product"
}

object AdminCategoryDestination : AppDestinations {
    override val route = "admin_categories"
}

object AdminOrderListDestination : AppDestinations {
    override val route = "admin_orders"
}

object AdminUserListDestination : AppDestinations {
    override val route = "admin_users"
}

object CategoryProductsDestination : AppDestinations {
    override val route = "category_products"
}

