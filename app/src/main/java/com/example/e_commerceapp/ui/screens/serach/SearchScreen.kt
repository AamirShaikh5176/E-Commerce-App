package com.example.e_commerceapp.ui.screens.serach

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.mapper.toFavoriteProductEntity
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.serach.component.SearchItem
import com.example.e_commerceapp.ui.screens.serach.component.SearchTextField

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onProductClicked: (SearchProductEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    // call function when change the searchQuery
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearSearchResult()
        } else {
            viewModel.getProductsByQuery(searchQuery)
        }
    }

    HideNavigationBarOnly()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp)
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


            SearchTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                },
                onFilterClick = { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            searchQuery.isBlank() -> {
                EmptySearchImage()
            }

            searchState.isLoading -> {
                Loading()
            }

            searchState.errorMessage != null -> {
                SearchError(errorMessage = searchState.errorMessage.toString())
            }

            searchState.data != null -> {
                searchState.data?.let { products ->
                    LazyColumn {
                        items(
                            items = products,
                            key = { it.id }
                        ) { searchProductEntity ->
                            SearchItem(
                                searchProductEntity = searchProductEntity,
                                onItemClicked = onProductClicked,
                                onClickedFavorite = {
                                    toggleFavorite(searchProductEntity, viewModel, context)
                                },
                                isFavorite = searchProductEntity.isFavorite
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun EmptySearchImage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.shopping_bag),
            contentDescription = "Empty Search",
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.4f
        )
    }
}


@Composable
private fun SearchError(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                color = Color.Gray
            )
        }
    }
}


private fun toggleFavorite(
    searchProductEntity: SearchProductEntity,
    viewModel: SearchViewModel,
    context: Context,
) {
    if (searchProductEntity.isFavorite) {
        viewModel.deleteFavoriteProduct(searchProductEntity.toFavoriteProductEntity())
        Toast.makeText(context, "Product removed from favorites", Toast.LENGTH_SHORT).show()
    } else {
        viewModel.addFavoriteProduct(searchProductEntity.toFavoriteProductEntity())
        Toast.makeText(context, "Product added to favorites", Toast.LENGTH_SHORT).show()
    }
}