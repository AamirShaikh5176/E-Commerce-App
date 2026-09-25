package com.example.e_commerceapp.ui.screens.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.ui.screens.common.QuantityButton
import com.example.e_commerceapp.utils.CurrencyUtils

@Composable
fun CartItem(
    userCartEntity: UserCartEntity,
    onCartItemClicked: (UserCartEntity) -> Unit = {},
    onDeleteCartClicked: (UserCartEntity) -> Unit = {},
    onIncrementClicked: () -> Unit = {},
    onDecrementClicked: () -> Unit = {},
) {
    val estimatedMrp = remember(userCartEntity.price) {
        userCartEntity.price * 1.35
    }
    val (savings, discountPercent) = remember(estimatedMrp, userCartEntity.price) {
        CurrencyUtils.calculateSavings(estimatedMrp, userCartEntity.price)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onCartItemClicked(userCartEntity) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Product Image with rounded corners
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8F9FA))
                ) {
                    AsyncImage(
                        model = userCartEntity.image,
                        contentDescription = stringResource(id = R.string.product_image_content),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Product Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = userCartEntity.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = userCartEntity.description,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Indian Pricing Row
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = CurrencyUtils.formatInr(userCartEntity.price * userCartEntity.quantity),
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = CurrencyUtils.formatInr(estimatedMrp * userCartEntity.quantity),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "$discountPercent% off",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Stepper and Remove
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remove button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onDeleteCartClicked(userCartEntity) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Remove",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE53935)
                    )
                }

                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    QuantityButton(
                        icon = Icons.Default.Remove,
                        sizeBox = 26,
                        sizeIcon = 16,
                        onClick = onDecrementClicked
                    )

                    Text(
                        text = userCartEntity.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )

                    QuantityButton(
                        icon = Icons.Default.Add,
                        sizeBox = 26,
                        sizeIcon = 16,
                        onClick = onIncrementClicked
                    )
                }
            }
        }
    }
}