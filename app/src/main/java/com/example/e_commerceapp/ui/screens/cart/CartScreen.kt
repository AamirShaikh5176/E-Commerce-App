package com.example.e_commerceapp.ui.screens.cart

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.ui.screens.common.CheckoutBar
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.Loading
import java.util.Locale

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onCheckoutClicked: (String) -> Unit,
    onCartClicked: (UserCartEntity) -> Unit,
    onBackClick: () -> Unit,
    onBadgeCountChange: (Int) -> Unit,
) {
    val context = LocalContext.current

    val cartState by viewModel.userCartsState.collectAsStateWithLifecycle()
    val totalPriceState by viewModel.totalPriceState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var deletedCart by remember { mutableStateOf<UserCartEntity?>(null) }

    LaunchedEffect(deletedCart) {
        deletedCart?.let { userCartEntity ->

            val result = snackbarHostState.showSnackbar(
                message = "Item removed from Carts",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.addUserCartItem(userCartEntity)
                viewModel.calculateTotalPrice()

                Toast.makeText(context, "Item added back to Carts", Toast.LENGTH_SHORT).show()
            }
            // delete value to not repeat
            deletedCart = null
        }
    }
    val onDeleteCartClicked = { userCartEntity: UserCartEntity ->
        viewModel.deleteUserCartItem(userCartEntity)
        viewModel.calculateTotalPrice()
        deletedCart = userCartEntity
//        onBadgeCountChange(viewModel.badgeCount.value.minus(1))
    }

    val onIncrement = { userCartEntity: UserCartEntity ->
        if (userCartEntity.quantity < userCartEntity.availableQuantity) {
            viewModel.incrementQuantityOptimistic(userCartEntity)
            viewModel.calculateTotalPrice()
        } else {
            Toast.makeText(context, "Maximum quantity currently available", Toast.LENGTH_SHORT)
                .show()
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
            SnackbarHost(
                hostState = snackbarHostState
            ) { snackbarData ->
                Snackbar(
                    containerColor = colorResource(R.color.dark_blue),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp),
                    action = {
                        snackbarData.visuals.actionLabel?.let { actionLabel ->
                            TextButton(
                                onClick = { snackbarData.performAction() }
                            ) {
                                Text(
                                    text = actionLabel,
                                    color = Color.Yellow
                                )
                            }
                        }
                    }
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        fontSize = 16.sp,
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
                SuccessScreen(
                    listUserCartEntity = listUserCartEntity,
                    modifier = Modifier,
                    onBackClick = { onBackClick() },
                    onNotificationClick = {},
                    onCartClicked = onCartClicked,
                    onDeleteCartClicked = onDeleteCartClicked,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    onCheckoutClicked = onCheckoutClicked,
                    totalPrice = totalPriceState,
                )
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
    onNotificationClick: () -> Unit,
    onCartClicked: (UserCartEntity) -> Unit,
    onDeleteCartClicked: (UserCartEntity) -> Unit,
    onIncrement: (UserCartEntity) -> Unit,
    onDecrement: (UserCartEntity) -> Unit,
    onCheckoutClicked: (String) -> Unit,
) {
    val totalPriceText = String.format(Locale.ENGLISH, "%.2f", totalPrice)

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Back Icon
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }

                // Title
                Text(
                    text = "My Cart",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.medium))
                )

                // Notification Icon
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listUserCartEntity) { userCartEntity ->
                    CartItem(
                        userCartEntity = userCartEntity,
                        onCartItemClicked = onCartClicked,
                        onDeleteCartClicked = onDeleteCartClicked,
                        onIncrementClicked = {
                            onIncrement(userCartEntity)
                        },
                        onDecrementClicked = {
                            onDecrement(userCartEntity)
                        },
                    )
                }
            }

        }

        CheckoutBar(
            totalPrice = totalPriceText,
            textButton = "Go To Checkout",
            onButtonClicked = { onCheckoutClicked(totalPriceText) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }
}


@Preview(showBackground = true)
@Composable
fun SuccessScreenPreview() {
    SuccessScreen(
        listUserCartEntity = listOf(
            UserCartEntity(
                productId = 1,
                title = "Product 1",
                description = "The Essence Mascara Lash Princess is a popular mascara known for its volumizing",
                price = 10.0,
                rating = "5",
                image = "",
                quantity = 1,
                availableQuantity = 20
            ),
            UserCartEntity(
                productId = 2,
                title = "Product 2",
                description = "The Essence Mascara Lash Princess is a popular mascara known for its volumizing",
                price = 20.0,
                rating = "4",
                image = "",
                quantity = 1,
                availableQuantity = 12
            )
        ),
        onBackClick = {},
        onNotificationClick = {},
        onCheckoutClicked = {},
        onCartClicked = {},
        onDeleteCartClicked = {},
        totalPrice = 303.90,
        onDecrement = {},
        onIncrement = {},
    )
}