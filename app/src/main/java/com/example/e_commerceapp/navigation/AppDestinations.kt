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
