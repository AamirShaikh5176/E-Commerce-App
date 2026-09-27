package com.example.e_commerceapp.ui.screens.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.utils.CurrencyUtils
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ProductItem(
    product: ProductEntity,
    modifier: Modifier = Modifier,
    onProductClicked: (ProductEntity) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(4.dp)
            .clickable { onProductClicked(product) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(colorResource(R.color.light_gray))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Product Name (controlled 2-line layout with consistent height)
            Text(
                text = product.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                minLines = 2,
                style = TextStyle(fontSize = 13.sp, lineHeight = 17.sp),
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily(Font(R.font.medium)),
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Rating Pill & Assured Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (product.rating >= 4.0) Color(0xFF388E3C) else Color(0xFFF57C00),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", product.rating)} ★",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (product.rating >= 4.0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2874F0).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✦ Assured",
                            color = Color(0xFF2874F0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Indian Rupee Price, MRP & Discount Badge (Universal 2-Line Layout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .defaultMinSize(minHeight = 36.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Line 1: Selling Price (Full width, bold, 0% clipping)
                Text(
                    text = CurrencyUtils.formatInr(product.price),
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.medium)),
                    maxLines = 1,
                    softWrap = false
                )

                // Line 2: MRP (strikethrough) + Discount Badge (horizontal, intact)
                if (product.mrpPrice > product.price || product.discountPercentage > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (product.mrpPrice > product.price) {
                            Text(
                                text = CurrencyUtils.formatInr(product.mrpPrice),
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (product.discountPercentage > 0) {
                            if (product.mrpPrice > product.price) {
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${product.discountPercentage.toInt()}% OFF",
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 4. Delivery & COD info
            Text(
                text = "Free Delivery • ${if (product.isCodAvailable) "COD Available" else "Prepaid"}",
                color = Color(0xFF059669),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}


@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val icon = when {
                rating >= i -> Icons.Filled.Star
                rating >= i - 0.5f -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(starSize)
            )
        }
    }
}


@Composable
@Preview
fun PreviewProductItem() {
    ProductItem(
        product = ProductEntity(
            id = "1",
            title = "Product 1",
            description = "Product 1 description",
            rating = 4.5,
            price = 10.0,
            imageUrl = "imageString",
            quantity = 1,
            discountPercentage = 10.0
        ),
        onProductClicked = {},
    )
}

