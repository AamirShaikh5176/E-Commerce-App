package com.example.e_commerceapp.ui.screens.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_commerceapp.data.dto.Category

/**
 * Story-style circular category pill inspired by modern Indian e-commerce apps (Flipkart/Myntra).
 */
@Composable
fun CategoryItem(
    isSelected: Boolean,
    category: Category,
    onCategoryClicked: (String) -> Unit
) {
    val activeGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onCategoryClicked(category.name) }
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC))
                .then(
                    if (isSelected) {
                        Modifier.border(2.5.dp, activeGradient, CircleShape)
                    } else {
                        Modifier.border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                category.slug.contains("smartphones") || category.slug.contains("laptop") -> Icons.Default.Devices
                category.slug.contains("beauty") || category.slug.contains("fragrance") || category.slug.contains("skin") -> Icons.Default.Face
                category.slug.contains("home") || category.slug.contains("furniture") || category.slug.contains("kitchen") -> Icons.Default.Home
                category.slug.contains("fashion") || category.slug.contains("dress") || category.slug.contains("shirt") || category.slug.contains("shoes") -> Icons.Default.LocalMall
                else -> Icons.Default.Category
            }

            Icon(
                imageVector = icon,
                contentDescription = category.name,
                tint = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF334155),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
