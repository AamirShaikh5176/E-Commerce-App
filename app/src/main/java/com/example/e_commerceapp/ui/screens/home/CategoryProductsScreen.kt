package com.example.e_commerceapp.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.ui.screens.home.component.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoryTitle: String,
    onProductClicked: (ProductEntity) -> Unit,
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val allProductsState by viewModel.allProductsState.collectAsStateWithLifecycle()
    val allProducts = allProductsState.data ?: emptyList()

    var selectedSortOption by remember { mutableStateOf("All") }

    // Filter products matching categoryTitle
    val filteredProducts = remember(allProducts, categoryTitle, selectedSortOption) {
        val baseList = when {
            categoryTitle.equals("All Products", ignoreCase = true) ||
            categoryTitle.equals("All", ignoreCase = true) -> {
                allProducts
            }
            categoryTitle.contains("Popular", ignoreCase = true) ||
            categoryTitle.contains("Trending", ignoreCase = true) -> {
                allProducts.sortedByDescending { it.rating }
            }
            categoryTitle.contains("Deals", ignoreCase = true) ||
            categoryTitle.contains("Savings", ignoreCase = true) ||
            categoryTitle.contains("Discount", ignoreCase = true) -> {
                allProducts.filter { it.discountPercentage >= 10.0 }
                    .sortedByDescending { it.discountPercentage }
            }
            categoryTitle.contains("Recommend", ignoreCase = true) -> {
                allProducts.filter { it.rating >= 4.3 }.ifEmpty { allProducts }
            }
            else -> {
                val term = categoryTitle.trim().lowercase()
                val matched = allProducts.filter {
                    val cat = it.category.lowercase()
                    cat == term || cat.contains(term) || term.contains(cat) ||
                    it.title.lowercase().contains(term) ||
                    it.brand.lowercase().contains(term) ||
                    ((term.contains("mobile") || term.contains("smartphone") || term.contains("phone")) && (cat.contains("mobile") || cat.contains("smartphone"))) ||
                    ((term.contains("electronic") || term.contains("laptop") || term.contains("tablet")) && (cat.contains("electronic") || cat.contains("laptop") || cat.contains("tablet"))) ||
                    ((term.contains("fashion") || term.contains("clothing") || term.contains("wear") || term.contains("shoes")) && (cat.contains("fashion") || cat.contains("clothing"))) ||
                    ((term.contains("beauty") || term.contains("skincare") || term.contains("cosmetic")) && (cat.contains("beauty") || cat.contains("skincare"))) ||
                    ((term.contains("home") || term.contains("furniture") || term.contains("decor")) && (cat.contains("home") || cat.contains("furniture"))) ||
                    ((term.contains("grocer") || term.contains("food")) && (cat.contains("grocer") || cat.contains("food"))) ||
                    ((term.contains("sport") || term.contains("fitness")) && (cat.contains("sport") || cat.contains("fitness"))) ||
                    ((term.contains("accessor") || term.contains("watch")) && (cat.contains("accessor") || cat.contains("watch")))
                }
                if (matched.isNotEmpty()) matched else allProducts
            }
        }

        when (selectedSortOption) {
            "Price: Low to High" -> baseList.sortedBy { it.price }
            "Price: High to Low" -> baseList.sortedByDescending { it.price }
            "Top Rated" -> baseList.sortedByDescending { it.rating }
            "Biggest Discount" -> baseList.sortedByDescending { it.discountPercentage }
            else -> baseList
        }
    }

    val sortChips = listOf("All", "Price: Low to High", "Price: High to Low", "Top Rated", "Biggest Discount")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = categoryTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${filteredProducts.size} Products available",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sort Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortChips) { chip ->
                    val isSelected = selectedSortOption == chip
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSortOption = chip },
                        label = { Text(chip, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF334155)
                        )
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No products found for '$categoryTitle'",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try selecting a different category or clearing filters.",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("Explore Categories", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                val productPairs = filteredProducts.chunked(2)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(productPairs) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProductItem(
                                product = pair[0],
                                modifier = Modifier.weight(1f),
                                onProductClicked = onProductClicked
                            )
                            if (pair.size > 1) {
                                ProductItem(
                                    product = pair[1],
                                    modifier = Modifier.weight(1f),
                                    onProductClicked = onProductClicked
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
