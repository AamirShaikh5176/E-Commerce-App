package com.example.e_commerceapp.ui.screens.serach

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.e_commerceapp.R
import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.mapper.toFavoriteProductEntity
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.serach.component.SearchItem
import com.example.e_commerceapp.ui.screens.serach.component.SearchTextField
import com.example.e_commerceapp.utils.CurrencyUtils

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onProductClicked: (SearchProductEntity) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val recommendedProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()

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
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
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
                SearchRecommendations(
                    recommendedProducts = recommendedProducts,
                    categories = categories,
                    onQuerySelected = { query ->
                        searchQuery = query
                    },
                    onProductSelected = { product ->
                        onProductClicked(
                            SearchProductEntity(
                                id = product.id,
                                title = product.title,
                                description = product.description,
                                price = product.price,
                                imageUrl = product.imageUrl,
                                rating = product.rating,
                                quantity = product.quantity,
                                discountPercentage = product.discountPercentage,
                                isFavorite = false
                            )
                        )
                    }
                )
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
fun SearchRecommendations(
    recommendedProducts: List<ProductEntity>,
    categories: List<Category>,
    onQuerySelected: (String) -> Unit,
    onProductSelected: (ProductEntity) -> Unit,
) {
    val trendingSearches = listOf(
        "Mobiles", "Laptops", "Fashion", "Beauty",
        "Shoes", "Smart Watch", "Groceries", "Fragrances"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Trending Searches
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Trending Searches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trendingSearches) { term ->
                        SuggestionChip(
                            onClick = { onQuerySelected(term) },
                            label = { Text(term, fontSize = 13.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF1E293B)
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }
        }

        // 2. Shop by Category
        val displayCategories = if (categories.isNotEmpty()) {
            categories
        } else {
            listOf(
                Category("Mobiles", "smartphones", ""),
                Category("Laptops", "laptops", ""),
                Category("Fashion", "tops", ""),
                Category("Beauty", "skincare", ""),
                Category("Groceries", "groceries", ""),
                Category("Furniture", "furniture", "")
            )
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Shop by Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayCategories) { cat ->
                        Card(
                            onClick = { onQuerySelected(cat.name) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE0F2FE)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Recommended For You
        if (recommendedProducts.isNotEmpty()) {
            item {
                Text(
                    text = "Recommended For You",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(recommendedProducts.take(15)) { product ->
                Card(
                    onClick = { onProductSelected(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.title,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = product.category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyUtils.formatInr(product.price),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (product.mrpPrice > product.price) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = CurrencyUtils.formatInr(product.mrpPrice),
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${product.discountPercentage.toInt()}% off",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
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