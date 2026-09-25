package com.example.e_commerceapp.ui.screens.home.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryStyle(
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color,
    val borderColor: Color = backgroundColor.copy(alpha = 0.5f),
)

object CategoryColorConfig {

    fun getCategoryStyle(slugOrName: String): CategoryStyle {
        val s = slugOrName.lowercase()
        return when {
            // Beauty: pink/purple
            s.contains("beauty") || s.contains("skincare") -> CategoryStyle(
                icon = Icons.Default.Face,
                backgroundColor = Color(0xFFFCE7F3), // Pastel pink
                iconColor = Color(0xFFDB2777),       // Deep pink
                borderColor = Color(0xFFF472B6)
            )

            // Fragrances: violet
            s.contains("fragrance") || s.contains("perfume") -> CategoryStyle(
                icon = Icons.Default.Spa,
                backgroundColor = Color(0xFFEDE9FE), // Pastel violet
                iconColor = Color(0xFF7C3AED),       // Deep violet
                borderColor = Color(0xFFA78BFA)
            )

            // Furniture: brown/orange
            s.contains("furniture") || s.contains("chair") || s.contains("table") -> CategoryStyle(
                icon = Icons.Default.Weekend,
                backgroundColor = Color(0xFFFFEDD5), // Soft orange/brown
                iconColor = Color(0xFFEA580C),       // Rust orange
                borderColor = Color(0xFFFB923C)
            )

            // Groceries: green
            s.contains("grocer") || s.contains("food") -> CategoryStyle(
                icon = Icons.Default.Restaurant,
                backgroundColor = Color(0xFFDCFCE7), // Soft green
                iconColor = Color(0xFF16A34A),       // Forest green
                borderColor = Color(0xFF4ADE80)
            )

            // Home: teal
            s.contains("home") || s.contains("kitchen") || s.contains("decoration") -> CategoryStyle(
                icon = Icons.Default.Home,
                backgroundColor = Color(0xFFCCFBF1), // Soft teal
                iconColor = Color(0xFF0D9488),       // Teal
                borderColor = Color(0xFF2DD4BF)
            )

            // Laptops: dark blue
            s.contains("laptop") || s.contains("computer") -> CategoryStyle(
                icon = Icons.Default.Laptop,
                backgroundColor = Color(0xFFE0E7FF), // Soft indigo
                iconColor = Color(0xFF1E3A8A),       // Dark blue
                borderColor = Color(0xFF818CF8)
            )

            // Smartphones: indigo
            s.contains("smartphone") || s.contains("mobile") || s.contains("phone") -> CategoryStyle(
                icon = Icons.Default.PhoneAndroid,
                backgroundColor = Color(0xFFE0E7FF), // Soft indigo
                iconColor = Color(0xFF4338CA),       // Indigo
                borderColor = Color(0xFF6366F1)
            )

            // Electronics: blue
            s.contains("electronic") || s.contains("gadget") || s.contains("device") -> CategoryStyle(
                icon = Icons.Default.Laptop,
                backgroundColor = Color(0xFFE0F2FE), // Soft blue
                iconColor = Color(0xFF0284C7),       // Sky blue
                borderColor = Color(0xFF38BDF8)
            )

            // Fashion: purple
            s.contains("fashion") || s.contains("shirt") || s.contains("dress") || s.contains("shoe") || s.contains("watch") || s.contains("tops") || s.contains("clothing") -> CategoryStyle(
                icon = Icons.Default.LocalMall,
                backgroundColor = Color(0xFFF3E8FF), // Soft purple
                iconColor = Color(0xFF9333EA),       // Deep purple
                borderColor = Color(0xFFC084FC)
            )

            // Sports: orange
            s.contains("sport") || s.contains("fitness") || s.contains("gym") -> CategoryStyle(
                icon = Icons.Default.FitnessCenter,
                backgroundColor = Color(0xFFFFEDD5), // Soft orange
                iconColor = Color(0xFFD97706),       // Amber/orange
                borderColor = Color(0xFFFBBF24)
            )

            // Automotive: red
            s.contains("auto") || s.contains("vehicle") || s.contains("motorcycle") -> CategoryStyle(
                icon = Icons.Default.DirectionsCar,
                backgroundColor = Color(0xFFFEE2E2), // Soft red
                iconColor = Color(0xFFDC2626),       // Red
                borderColor = Color(0xFFF87171)
            )

            // Books: amber
            s.contains("book") || s.contains("stationery") -> CategoryStyle(
                icon = Icons.Default.AutoStories,
                backgroundColor = Color(0xFFFEF3C7), // Soft amber
                iconColor = Color(0xFFB45309),       // Amber
                borderColor = Color(0xFFFCD34D)
            )

            // Default fallback
            else -> CategoryStyle(
                icon = Icons.Default.Category,
                backgroundColor = Color(0xFFF1F5F9), // Slate
                iconColor = Color(0xFF475569),       // Slate
                borderColor = Color(0xFF94A3B8)
            )
        }
    }
}
