package com.example.e_commerceapp.ui.screens.details

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.common.QuantityButton
import com.example.e_commerceapp.utils.CurrencyUtils

@Composable
fun ProductDetailScreen(
    productId: String,
    onBuyNowClick: (ProductDetailsEntity) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: (ProductDetailsEntity) -> Unit = {},
    onCreateAccountClick: (ProductDetailsEntity) -> Unit = {},
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    HideNavigationBarOnly()

    val context = LocalContext.current
    var showGuestPrompt by remember { mutableStateOf(false) }
    var pendingProduct by remember { mutableStateOf<ProductDetailsEntity?>(null) }

    if (showGuestPrompt) {
        com.example.e_commerceapp.ui.screens.common.GuestLoginRequiredDialog(
            onDismissRequest = {
                showGuestPrompt = false
                pendingProduct = null
            },
            onLoginClick = {
                showGuestPrompt = false
                pendingProduct?.let { onLoginClick(it) }
            },
            onCreateAccountClick = {
                showGuestPrompt = false
                pendingProduct?.let { onCreateAccountClick(it) }
            }
        )
    }

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
            onBuyNowClick = { productEntity ->
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                android.util.Log.d(
                    "BuyNowAuth",
                    "ProductDetails onBuyNowClick: currentUser != null: ${currentUser != null}, uid: ${currentUser?.uid}, email: ${currentUser?.email}"
                )
                if (currentUser == null) {
                    pendingProduct = productEntity
                    showGuestPrompt = true
                } else {
                    onBuyNowClick(productEntity)
                }
            },
            onBackClick = onBackClick,
            isFavorite = isFavorite,
            isAddedToCart = isAddedToCart,
            onClickFavorite = { productDetailsEntity ->
                isFavorite = !isFavorite
                if (isFavorite) {
                    viewModel.addToFavorite(productDetailsEntity)
                    showToast("Added to wishlist", context)
                } else {
                    viewModel.removeFromFavorite(productDetailsEntity)
                    showToast("Removed from wishlist", context)
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
    val focusManager = LocalFocusManager.current

    // Indian PIN Code checker state
    var pinCodeInput by remember { mutableStateOf("560001") }
    var deliveryEstimateText by remember { mutableStateOf("Free Delivery by Tomorrow, 11 PM") }
    var pinCheckSuccess by remember { mutableStateOf(true) }

    val images = remember(product.imageUrl) {
        if (product.imageUrl.isNotEmpty()) product.imageUrl else listOf("")
    }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { images.size })

    val (savingsAmount, discountPercent) = remember(product.mrpPrice, product.price) {
        val calc = CurrencyUtils.calculateSavings(product.mrpPrice, product.price)
        if (calc.second > 0) calc else Pair((product.price * 0.35), 26)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // space for sticky bottom bar
        ) {
            // Header with back button and wishlist icon
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.bg_product_details))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(22.dp),
                                tint = Color.Black
                            )
                        }

                        // Brand Badge if available
                        if (product.brand.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Text(
                                    text = product.brand.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colorResource(R.color.dark_blue),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onClickFavorite(product) },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-Image Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val img = images.getOrNull(page).orEmpty()
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(img),
                                    contentDescription = product.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        // Discount badge on top left of image
                        if (discountPercent > 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32)
                            ) {
                                Text(
                                    text = "$discountPercent% OFF",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Dot Indicators
                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(images.size) { index ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) colorResource(R.color.dark_blue)
                                            else Color.LightGray
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Main Product Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Product Title
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating & Reviews row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2E7D32)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.rating,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "1,428 Ratings & 235 Reviews",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Stock status
                    if (product.stockQuantity <= 5 && product.stockQuantity > 0) {
                        Text(
                            text = "Only ${product.stockQuantity} left in stock!",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    } else if (product.stockQuantity > 0) {
                        Text(
                            text = "In Stock",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Section (Indian Rupee with MRP & Savings)
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = CurrencyUtils.formatInr(product.price),
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = CurrencyUtils.formatInr(product.mrpPrice),
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "Save ${CurrencyUtils.formatInr(savingsAmount)} ($discountPercent% OFF)",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Inclusive of all taxes",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Indian PIN Code Delivery Checker Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Delivery",
                                tint = colorResource(R.color.dark_blue),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Check Delivery Options",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pinCodeInput,
                                onValueChange = { if (it.length <= 6) pinCodeInput = it.filter { ch -> ch.isDigit() } },
                                placeholder = { Text("Enter 6-digit PIN", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (pinCodeInput.length == 6) {
                                            pinCheckSuccess = true
                                            deliveryEstimateText = "Delivery by Tomorrow, 11 PM to $pinCodeInput"
                                        } else {
                                            pinCheckSuccess = false
                                            deliveryEstimateText = "Please enter valid 6-digit Indian PIN"
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedBorderColor = colorResource(R.color.dark_blue),
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (pinCodeInput.length == 6) {
                                        pinCheckSuccess = true
                                        deliveryEstimateText = "Delivery by Tomorrow, 11 PM to $pinCodeInput"
                                    } else {
                                        pinCheckSuccess = false
                                        deliveryEstimateText = "Please enter valid 6-digit Indian PIN"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.dark_blue)
                                ),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text("Check", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (pinCheckSuccess) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = if (pinCheckSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = deliveryEstimateText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (pinCheckSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (product.isCodAvailable) {
                                Text(
                                    text = "✔ Cash on Delivery Available",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            Text(
                                text = "✔ 7-Day Replacement Policy",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Available Bank & Festive Offers Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFFFE082))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = "Offers",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Available Offers & Discounts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFB78103)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OfferBullet("Bank Offer", "10% Instant Discount on SBI and HDFC Bank Cards up to ₹1,500.")
                        OfferBullet("UPI Offer", "Flat ₹50 cashback on payments via Google Pay or PhonePe.")
                        OfferBullet("Coupon Code", "Use code 'FESTIVE100' at checkout to get ₹100 instant off.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specifications Section
                Text(
                    text = "Product Specifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SpecRow("Brand", product.brand.ifBlank { "Original Essentials" })
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        SpecRow("Stock Available", "${product.stockQuantity} units")
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        SpecRow("Standard Delivery", "${product.deliveryDays} business days")
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        SpecRow("Cash on Delivery", if (product.isCodAvailable) "Available" else "Not Available")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = product.description,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quantity Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select Quantity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(4.dp)
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
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
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
                                    showToast("Maximum available quantity reached", context)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Modern Sticky Dual Bottom Bar: Add to Cart + Buy Now
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 10.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add to Cart Button (Outlined / Secondary)
                OutlinedButton(
                    onClick = { onClickCart(product.copy(quantity = quantity)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, colorResource(R.color.dark_blue)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.dark_blue)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAddedToCart) "In Cart" else "Add to Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Buy Now Button (Primary Vibrant)
                Button(
                    onClick = { onBuyNowClick(product.copy(quantity = quantity)) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800) // Modern Flipkart/Amazon orange action color
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Buy Now",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buy Now • ${CurrencyUtils.formatInr(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OfferBullet(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 12.sp)
        Text(
            text = "$title: ",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E3A48),
            fontSize = 12.sp
        )
        Text(
            text = desc,
            color = Color(0xFF555555),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
    }
}

private fun showToast(mes: String, context: Context) {
    Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
}
