package com.example.e_commerceapp.ui.screens.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.e_commerceapp.data.dto.Category

@Composable
fun CategoryItem(isSelected: Boolean, category: Category, onCategoryClicked: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFFE0E0E0)
            else
                Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color.Black else Color.Gray
        ),
        modifier = Modifier.clickable {
            onCategoryClicked(category.name)
        }
    ) {
        Text(
            text = category.name,
            color = if (isSelected) Color.Black else Color.DarkGray,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
            fontWeight = FontWeight.SemiBold
        )
    }
}

