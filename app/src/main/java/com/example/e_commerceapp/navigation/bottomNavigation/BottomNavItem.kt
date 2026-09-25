package com.example.e_commerceapp.navigation.bottomNavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.e_commerceapp.navigation.CartScreen
import com.example.e_commerceapp.navigation.FavoriteScreen
import com.example.e_commerceapp.navigation.HomeScreen
import com.example.e_commerceapp.navigation.ProfileScreen

sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
) {
    object Home : BottomNavItem(
        title = "Home",
        icon = Icons.Default.Home,
        route = HomeScreen.route,
    )

    object Cart : BottomNavItem(
        title = "Cart",
        icon = Icons.Default.ShoppingCart,
        route = CartScreen.route,
    )

    object Favorite : BottomNavItem(
        title = "Favorite",
        icon = Icons.Default.Favorite,
        route = FavoriteScreen.route,
    )

    object Profile : BottomNavItem(
        title = "Profile",
        icon = Icons.Default.Person,
        route = ProfileScreen.route,
    )
}
