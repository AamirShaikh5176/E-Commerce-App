package com.example.e_commerceapp.ui.screens.details

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.ui.screens.common.CheckoutBar
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.common.QuantityButton
import com.example.e_commerceapp.ui.screens.common.ShowToast
import java.util.Locale


@Composable
fun ProductDetailScreen(
    productId: Int,
    onBuyNowClick: (ProductDetailsEntity) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    HideNavigationBarOnly()

    val context = LocalContext.current

    LaunchedEffect(productId) {
        viewModel.getProductDetails(productId)
    }

    val productDetailsState by viewModel.productDetailsState.collectAsStateWithLifecycle()

    if (productDetailsState.isLoading) {
        Loading()
    }

    productDetailsState.data?.let { product ->
        var isFavorite by remember { mutableStateOf(product.isFavorite) }
        var isAddedToCart by remember { mutableStateOf(product.isAddedToCart) }

        ProductDetailsDesign(
            product = product,
            onBuyNowClick = onBuyNowClick,
            onBackClick = onBackClick,
            isFavorite = isFavorite,
            isAddedToCart = isAddedToCart,
            onClickFavorite = { productDetailsEntity ->
                isFavorite = !isFavorite
                if (isFavorite) {
                    viewModel.addToFavorite(productDetailsEntity)
                    showToast("Added to favorites", context)
                } else {
                    viewModel.removeFromFavorite(productDetailsEntity)
                    showToast("Removed from favorites", context)
                }
            },
            onClickCart = { productDetailsEntity ->
                isAddedToCart = !isAddedToCart
                if (isAddedToCart) {
                    viewModel.addToCart(productDetailsEntity)
                    showToast("Added to cart", context)
                } else {
                    viewModel.removeFromCart(productDetailsEntity)
                    showToast("Removed from cart", context)
                }
            }
        )
    }

    if (productDetailsState.errorMessage != null) {
        ErrorMessage(productDetailsState.errorMessage.toString())
    }

}


@Composable
fun ProductDetailsDesign(
    product: ProductDetailsEntity,
    isFavorite: Boolean,
    isAddedToCart: Boolean,
    onBuyNowClick: (ProductDetailsEntity) -> Unit,
    onClickFavorite: (ProductDetailsEntity) -> Unit,
    onClickCart: (ProductDetailsEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    var quantity by remember { mutableIntStateOf(1) }

    var totalPrice by remember { mutableDoubleStateOf(product.price) }

    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.bg_product_details))
                        .padding(16.dp)
                ) {

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = { onClickFavorite(product) },
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) colorResource(R.color.main_color) else Color.Gray,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (product.imageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(product.imageUrl.first()),
                                contentDescription = product.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily(Font(R.font.medium)),
                        modifier = Modifier
                            .weight(3f)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onClickCart(product.copy(quantity = quantity))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isAddedToCart)
                                        R.drawable.cart_fill
                                    else
                                        R.drawable.cart_border
                                ),
                                contentDescription = "Cart",
                                tint = if (isAddedToCart)
                                    colorResource(R.color.main_color) else Color.Gray,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }



                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = product.rating, fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.cairo_regular)),
                    )

                    Text(
                        text = "    |    ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.cairo_regular)),
                    )

                    Text(
                        text = "${product.discountPercentage}% Discount",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.cairo_regular)),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${product.price} EGP",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    fontFamily = FontFamily(Font(R.font.cairo_regular)),
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    thickness = 2.dp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                Text(
                    text = "Description",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(
                        Font(R.font.medium)
                    ),
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Description
                Text(
                    text = product.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(
                        Font(R.font.cairo_regular)
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))
                // Specify the required quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(
                            Font(R.font.medium)
                        ),
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuantityButton(
                            icon = Icons.Default.Remove,
                            sizeBox = 32,
                            sizeIcon = 18,
                            onClick = {
                                if (quantity > 1) {
                                    quantity--
                                    totalPrice = product.price * quantity
                                }
                            }
                        )

                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 18.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        QuantityButton(
                            icon = Icons.Default.Add,
                            sizeBox = 32,
                            sizeIcon = 18,
                            onClick = {
                                if (quantity < product.availableQuantity) {
                                    quantity++
                                    totalPrice = product.price * quantity
                                } else {
                                    showToast("Maximum quantity currently available", context)
                                }
                            }
                        )
                    }

                }

                Spacer(modifier = Modifier.height(120.dp))
            }

        }

        CheckoutBar(
            totalPrice = String.format(Locale.ENGLISH, "%.2f", totalPrice),
            textButton = "Buy Now",
            onButtonClicked = { onBuyNowClick(product.copy(quantity = quantity)) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


private fun showToast(mes: String, context: Context) {
    Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    ProductDetailScreen(
        productId = 5,
        onBuyNowClick = {},
        onBackClick = {},
    )
}
