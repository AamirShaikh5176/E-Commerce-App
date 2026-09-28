package com.example.e_commerceapp.ui.screens.favorite

import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.common.ShowToast


@Composable
fun FavoriteScreen(
    onClickedFavoriteProduct: (FavoriteProductEntity) -> Unit,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val favoriteState by viewModel.favoriteState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshFavorites()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var deletedFavoriteProduct by remember { mutableStateOf<FavoriteProductEntity?>(null) }

    LaunchedEffect(deletedFavoriteProduct) {
        deletedFavoriteProduct?.let { product ->

            val result = snackbarHostState.showSnackbar(
                message = "Item removed from favorites",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.addFavoriteProduct(product)

                Toast.makeText(context, "Item added back to favorites", Toast.LENGTH_SHORT).show()
            }
            // delete value to not repeat
            deletedFavoriteProduct = null
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
            if (favoriteState.isLoading) {
                Loading()
            }

            favoriteState.data?.let { listFavoriteProductEntity ->
                FavoriteScreenContent(
                    listFavoriteProductEntity = listFavoriteProductEntity,
                    onClickedFavoriteProduct = onClickedFavoriteProduct,
                    onBackClick = { onBackClick() },
                    onNotificationClick = { onNotificationClick() },
                    onClickedDeleteFavorite = { favoriteProductEntity ->
                        viewModel.deleteFavoriteProduct(favoriteProductEntity)
                        deletedFavoriteProduct = favoriteProductEntity
                    }
                )
            }

            if (favoriteState.errorMessage != null) {
                ShowToast(mes = favoriteState.errorMessage.toString())
            }
        }
    }
}


@Composable
fun FavoriteScreenContent(
    listFavoriteProductEntity: List<FavoriteProductEntity>,
    onClickedFavoriteProduct: (FavoriteProductEntity) -> Unit,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onClickedDeleteFavorite: (FavoriteProductEntity) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Back Icon
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(28.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                    )
                }

                // Title
                Text(
                    text = "Favorites",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily(Font(R.font.medium))
                )

                // Notification Icon
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (listFavoriteProductEntity.isEmpty()) {
                // Empty Favorites State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "❤️",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No favorites yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily(Font(R.font.medium))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Save products you love and find them here later.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    androidx.compose.material3.Button(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.main_color)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Continue Shopping",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listFavoriteProductEntity) { favoriteProductEntity ->
                        FavoriteItem(
                            favoriteProductEntity = favoriteProductEntity,
                            onItemClick = onClickedFavoriteProduct,
                            onClickedDeleteFavorite = onClickedDeleteFavorite
                        )
                    }
                }
            }
        }
    }
}