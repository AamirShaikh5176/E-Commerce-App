package com.example.e_commerceapp.ui.screens.cart

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.utils.CurrencyUtils

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onCheckoutClicked: (String) -> Unit,
    onCartClicked: (UserCartEntity) -> Unit,
    onBackClick: () -> Unit,
    onBadgeCountChange: (Int) -> Unit,
    onLoginClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var showGuestPrompt by remember { mutableStateOf(false) }

    if (showGuestPrompt) {
        com.example.e_commerceapp.ui.screens.common.GuestLoginRequiredDialog(
            onDismissRequest = { showGuestPrompt = false },
            onLoginClick = {
                showGuestPrompt = false
                onLoginClick()
            },
            onCreateAccountClick = {
                showGuestPrompt = false
                onCreateAccountClick()
            }
        )
    }

    val cartState by viewModel.userCartsState.collectAsStateWithLifecycle()
    val totalPriceState by viewModel.totalPriceState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var deletedCart by remember { mutableStateOf<UserCartEntity?>(null) }

    LaunchedEffect(deletedCart) {
        deletedCart?.let { userCartEntity ->
            val result = snackbarHostState.showSnackbar(
                message = "Item removed from Cart",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.addUserCartItem(userCartEntity)
                viewModel.calculateTotalPrice()
                Toast.makeText(context, "Item restored to Cart", Toast.LENGTH_SHORT).show()
            }
            deletedCart = null
        }
    }

    val onDeleteCartClicked = { userCartEntity: UserCartEntity ->
        viewModel.deleteUserCartItem(userCartEntity)
        viewModel.calculateTotalPrice()
        deletedCart = userCartEntity
    }

    val onIncrement = { userCartEntity: UserCartEntity ->
        if (userCartEntity.quantity < userCartEntity.availableQuantity) {
            viewModel.incrementQuantityOptimistic(userCartEntity)
            viewModel.calculateTotalPrice()
        } else {
            Toast.makeText(context, "Maximum quantity reached", Toast.LENGTH_SHORT).show()
        }
    }

    val onDecrement = { userCartEntity: UserCartEntity ->
        if (userCartEntity.quantity > 1) {
            viewModel.decrementQuantityOptimistic(userCartEntity)
            viewModel.calculateTotalPrice()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    containerColor = colorResource(R.color.dark_blue),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp),
                    action = {
                        snackbarData.visuals.actionLabel?.let { actionLabel ->
                            TextButton(onClick = { snackbarData.performAction() }) {
                                Text(text = actionLabel, color = Color.Yellow)
                            }
                        }
                    }
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cartState.isLoading) {
                Loading()
            }

            cartState.data?.let { listUserCartEntity ->
                if (listUserCartEntity.isEmpty()) {
                    EmptyCartView(onBackClick = onBackClick)
                } else {
                    SuccessScreen(
                        listUserCartEntity = listUserCartEntity,
                        modifier = Modifier,
                        onBackClick = onBackClick,
                        onCartClicked = onCartClicked,
                        onDeleteCartClicked = onDeleteCartClicked,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement,
                        onCheckoutClicked = { price ->
                            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            if (currentUser == null) {
                                showGuestPrompt = true
                            } else {
                                onCheckoutClicked(price)
                            }
                        },
                        totalPrice = totalPriceState,
                    )
                }
            }

            if (cartState.errorMessage != null) {
                ErrorMessage(cartState.errorMessage.toString())
            }
        }
    }
}

@Composable
fun SuccessScreen(
    modifier: Modifier = Modifier,
    listUserCartEntity: List<UserCartEntity>,
    totalPrice: Double,
    onBackClick: () -> Unit,
    onCartClicked: (UserCartEntity) -> Unit,
    onDeleteCartClicked: (UserCartEntity) -> Unit,
    onIncrement: (UserCartEntity) -> Unit,
    onDecrement: (UserCartEntity) -> Unit,
    onCheckoutClicked: (String) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Free delivery threshold
    val freeDeliveryThreshold = 499.0
    val isFreeDelivery = totalPrice >= freeDeliveryThreshold
    val amountNeededForFree = (freeDeliveryThreshold - totalPrice).coerceAtLeast(0.0)

    // Coupon discount logic
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<String?>(null) }
    var couponDiscount by remember { mutableDoubleStateOf(0.0) }

    fun applyCouponCode(code: String) {
        val trimmed = code.trim().uppercase()
        when (trimmed) {
            "FESTIVE100" -> {
                if (totalPrice >= 300) {
                    appliedCoupon = "FESTIVE100"
                    couponDiscount = 100.0
                    Toast.makeText(context, "Coupon FESTIVE100 applied: ₹100 Saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "FESTIVE100 requires minimum cart value of ₹300", Toast.LENGTH_SHORT).show()
                }
            }
            "WELCOME50" -> {
                appliedCoupon = "WELCOME50"
                couponDiscount = 50.0
                Toast.makeText(context, "Coupon WELCOME50 applied: ₹50 Saved!", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, "Invalid Coupon Code. Try 'FESTIVE100' or 'WELCOME50'", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val totalMrp = remember(listUserCartEntity) {
        listUserCartEntity.sumOf { (it.price * 1.35) * it.quantity }
    }
    val productDiscount = remember(totalMrp, totalPrice) {
        (totalMrp - totalPrice).coerceAtLeast(0.0)
    }
    val deliveryFee = if (isFreeDelivery) 0.0 else 40.0
    val finalTotal = (totalPrice - couponDiscount + deliveryFee).coerceAtLeast(0.0)
    val totalSavings = productDiscount + couponDiscount + (if (isFreeDelivery) 40.0 else 0.0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF1E293B)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shopping Cart",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "${listUserCartEntity.sumOf { it.quantity }} items",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Free Delivery Progress Banner
                item {
                    FreeDeliveryBanner(
                        isFreeDelivery = isFreeDelivery,
                        amountNeeded = amountNeededForFree,
                        progress = (totalPrice / freeDeliveryThreshold).coerceIn(0.0, 1.0).toFloat()
                    )
                }

                // Cart Items
                items(listUserCartEntity, key = { it.productId }) { userCartEntity ->
                    CartItem(
                        userCartEntity = userCartEntity,
                        onCartItemClicked = onCartClicked,
                        onDeleteCartClicked = onDeleteCartClicked,
                        onIncrementClicked = { onIncrement(userCartEntity) },
                        onDecrementClicked = { onDecrement(userCartEntity) },
                    )
                }

                // Coupon Application Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = "Coupon",
                                    tint = colorResource(R.color.dark_blue),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Apply Coupons & Offers",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9),
                                    border = BorderStroke(1.dp, Color(0xFF81C784)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "'$appliedCoupon' applied (${CurrencyUtils.formatInr(couponDiscount)} saved)",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }

                                        Text(
                                            text = "Remove",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFD32F2F),
                                            modifier = Modifier.clickable {
                                                appliedCoupon = null
                                                couponDiscount = 0.0
                                                couponInput = ""
                                            }
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it.uppercase() },
                                        placeholder = { Text("Enter FESTIVE100", fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                                applyCouponCode(couponInput)
                                            }
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedBorderColor = colorResource(R.color.dark_blue)
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            applyCouponCode(couponInput)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(R.color.dark_blue)
                                        ),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Apply", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Indian Price Details Breakdown Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PRICE DETAILS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            PriceSummaryRow(
                                label = "Price (${listUserCartEntity.sumOf { it.quantity }} items)",
                                value = CurrencyUtils.formatInr(totalMrp)
                            )

                            PriceSummaryRow(
                                label = "Discount on MRP",
                                value = "- ${CurrencyUtils.formatInr(productDiscount)}",
                                valueColor = Color(0xFF2E7D32)
                            )

                            if (couponDiscount > 0) {
                                PriceSummaryRow(
                                    label = "Coupon Discount ($appliedCoupon)",
                                    value = "- ${CurrencyUtils.formatInr(couponDiscount)}",
                                    valueColor = Color(0xFF2E7D32)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delivery Charges",
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569)
                                )
                                if (isFreeDelivery) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹40",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FREE",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "₹40",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Amount",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = CurrencyUtils.formatInr(finalTotal),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1B5E20)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🎉 You will save ${CurrencyUtils.formatInr(totalSavings)} on this order!",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                // Safe and secure badge
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Safe and Secure Payments • 100% Authentic Products",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Sticky Bottom Checkout Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 12.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Payable",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = CurrencyUtils.formatInr(finalTotal),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                }

                Button(
                    onClick = {
                        val formattedPrice = String.format(java.util.Locale.ENGLISH, "%.2f", finalTotal)
                        onCheckoutClicked(formattedPrice)
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text(
                        text = "Select Address  ➔",
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
private fun FreeDeliveryBanner(
    isFreeDelivery: Boolean,
    amountNeeded: Double,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFreeDelivery) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = if (isFreeDelivery) Color(0xFF2E7D32) else Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isFreeDelivery) {
                    Text(
                        text = "🎉 You've unlocked FREE Delivery on this order!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Text(
                        text = "Add ${CurrencyUtils.formatInr(amountNeeded)} more for FREE Delivery!",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }

            if (!isFreeDelivery) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFFFE0B2),
                )
            }
        }
    }
}

@Composable
private fun PriceSummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun EmptyCartView(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(50.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RemoveShoppingCart,
                contentDescription = "Empty Cart",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your Cart is Empty",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Looks like you haven't added anything to your cart yet. Explore our great festival deals now!",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.dark_blue)
            ),
            modifier = Modifier.height(48.dp)
        ) {
            Text("Start Shopping", fontWeight = FontWeight.Bold)
        }
    }
}