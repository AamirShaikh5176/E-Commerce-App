package com.example.e_commerceapp.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.home.component.CategoryItem
import com.example.e_commerceapp.ui.screens.home.component.HeaderHomeScreen
import com.example.e_commerceapp.ui.screens.home.component.ProductItem
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onProductClicked: (ProductEntity) -> Unit,
    onNotificationClick: () -> Unit,
    onClickSearchBar: () -> Unit,
    onCategoryClick: (String) -> Unit = {},
    onSeeAllCategoryClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeContent(
        modifier = Modifier,
        onProductClicked = onProductClicked,
        onNotificationClick = onNotificationClick,
        onCategoryClick = onCategoryClick,
        onSeeAllCategoryClick = onSeeAllCategoryClick,
        onClickSearchBar = onClickSearchBar,
        viewModel = viewModel,
    )
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    onProductClicked: (ProductEntity) -> Unit,
    onNotificationClick: () -> Unit,
    onCategoryClick: (String) -> Unit = {},
    onSeeAllCategoryClick: (String) -> Unit = {},
    onClickSearchBar: () -> Unit,
    viewModel: HomeViewModel,
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val categoryState by viewModel.categoryState.collectAsStateWithLifecycle()
    val allProductsState by viewModel.allProductsState.collectAsStateWithLifecycle()
    val selectedCategoryProducts by viewModel.productState.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var currentPinCode by remember { mutableStateOf("560038") }
    var currentCity by remember { mutableStateOf("Bengaluru") }
    var showPinDialog by remember { mutableStateOf(false) }
    var tempPinCode by remember { mutableStateOf(currentPinCode) }

    // Live countdown timer state (hours, minutes, seconds)
    var remainingSeconds by remember { mutableLongStateOf(15840L) }
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02dh : %02dm : %02ds", hours, minutes, seconds)

    HideNavigationBarOnly()

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Choose Delivery Location", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter your 6-digit PIN code to check delivery availability and courier speeds:",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = tempPinCode,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) tempPinCode = it },
                        label = { Text("6-Digit PIN Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPinCode.length == 6) {
                            currentPinCode = tempPinCode
                            currentCity = when (tempPinCode.take(2)) {
                                "56" -> "Bengaluru"
                                "11" -> "New Delhi"
                                "40" -> "Mumbai"
                                "60" -> "Chennai"
                                "70" -> "Kolkata"
                                "50" -> "Hyderabad"
                                else -> "India"
                            }
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Apply", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Catalog products and dynamic category shelves
    val allProducts = allProductsState.data ?: emptyList()
    val popularProducts = remember(allProducts) { allProducts.sortedByDescending { it.rating } }
    val bestDeals = remember(allProducts) {
        allProducts.filter { it.discountPercentage >= 10.0 }.sortedByDescending { it.discountPercentage }
    }
    val recommendedProducts = remember(allProducts) {
        allProducts.filter { it.rating >= 4.3 }.sortedByDescending { it.rating }
    }
    val categoryShelves = remember(allProducts, categoryState.data) {
        val cats = categoryState.data ?: emptyList()
        val grouped = allProducts.groupBy { it.category.trim() }
        grouped.map { (catRaw, prods) ->
            val matchingCat = cats.find {
                it.name.equals(catRaw, true) || it.slug.equals(catRaw, true)
            }
            val title = matchingCat?.name ?: catRaw.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            title to prods
        }.filter { it.second.isNotEmpty() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header (Greeting + Search Shortcut)
        item {
            HeaderHomeScreen(
                userInformationEntity = userState.data,
                onNotificationClick = onNotificationClick,
                onClickSearchBar = onClickSearchBar
            )
        }

        // 2. Delivery Location Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPinDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val customerName = userState.data?.name?.ifBlank { "Guest User" } ?: "Guest User"
                    Text(
                        text = "Deliver to $customerName, $currentPinCode • $currentCity",
                        color = Color(0xFF065F46),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Change",
                        color = Color(0xFF0284C7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Festival Carousel Banners
        item {
            val offers = listOf(
                R.drawable.image1,
                R.drawable.image2,
                R.drawable.image3,
                R.drawable.image4,
                R.drawable.image5,
            )

            val pagerState = rememberPagerState(pageCount = { offers.size })
            LaunchedEffect(Unit) {
                while (true) {
                    delay(3500)
                    val nextPage = (pagerState.currentPage + 1) % offers.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            Column {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                ) { page ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = offers[page]),
                            contentDescription = "Festival Offer Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(offers.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(if (isSelected) 18.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1))
                        )
                    }
                }
            }
        }

        // 4. Featured Categories (Horizontally Scrollable with Centralized Colors)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Categories",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily(Font(R.font.medium))
                    )

                    Text(
                        text = "See All →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        fontFamily = FontFamily(Font(R.font.cairo_regular)),
                        modifier = Modifier.clickable { onSeeAllCategoryClick("All Products") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (categoryState.data != null) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoryState.data!!) { category ->
                            val isSelected = selectedCategory == category.name

                            CategoryItem(
                                isSelected = isSelected,
                                category = category,
                                onCategoryClicked = {
                                    selectedCategory = category.name
                                    viewModel.getProductsByCategory(category.name)
                                    onCategoryClick(category.name)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Deal of the Day Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Deal of the Day",
                                color = Color(0xFF78350F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Special festival prices",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            color = Color(0xFFB45309),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Selected Category Filter Results (if user clicked a category)
        if (selectedCategory != null && selectedCategoryProducts.data?.isNotEmpty() == true) {
            item {
                ProductSection(
                    title = "$selectedCategory Collection",
                    products = selectedCategoryProducts.data ?: emptyList(),
                    onProductClicked = onProductClicked,
                    onSeeAllClicked = { onSeeAllCategoryClick(selectedCategory ?: "Products") }
                )
            }
        }

        // 7. Popular Products Section
        if (popularProducts.isNotEmpty()) {
            item {
                ProductSection(
                    title = "⚡ Trending & Popular Deals",
                    products = popularProducts.take(10),
                    onProductClicked = onProductClicked,
                    onSeeAllClicked = { onSeeAllCategoryClick("Popular Deals") }
                )
            }
        }

        // 8. Best Deals & Big Discounts Section
        if (bestDeals.isNotEmpty()) {
            item {
                ProductSection(
                    title = "🔥 Mega Savings & Discounts",
                    products = bestDeals.take(10),
                    onProductClicked = onProductClicked,
                    onSeeAllClicked = { onSeeAllCategoryClick("Best Deals") }
                )
            }
        }

        // 9. Dynamic Category Shelves (All categories with products)
        categoryShelves.forEach { shelf ->
            item(key = "category_shelf_${shelf.first}") {
                ProductSection(
                    title = "Best of ${shelf.first}",
                    products = shelf.second,
                    onProductClicked = onProductClicked,
                    onSeeAllClicked = { onSeeAllCategoryClick(shelf.first) }
                )
            }
        }

        // 10. Recommended For You Section
        if (recommendedProducts.isNotEmpty()) {
            item {
                ProductSection(
                    title = "✨ Recommended For You",
                    products = recommendedProducts.take(10),
                    onProductClicked = onProductClicked,
                    onSeeAllClicked = { onSeeAllCategoryClick("Recommended Products") }
                )
            }
        }

        // 11. Flipkart-style "Explore All Products" 2-Column Vertical Scroll Feed
        if (allProducts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore All Products",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily(Font(R.font.medium))
                    )
                    Text(
                        text = "${allProducts.size} items",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            val productPairs = allProducts.chunked(2)
            items(productPairs) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun ProductSection(
    title: String,
    products: List<ProductEntity>,
    onProductClicked: (ProductEntity) -> Unit,
    onSeeAllClicked: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily(Font(R.font.medium))
            )

            Text(
                text = "See All →",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7),
                fontFamily = FontFamily(Font(R.font.cairo_regular)),
                modifier = if (onSeeAllClicked != null) {
                    Modifier.clickable { onSeeAllClicked() }
                } else Modifier
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(products, key = { it.id }) { product ->
                ProductItem(
                    product = product,
                    modifier = Modifier.width(175.dp),
                    onProductClicked = onProductClicked
                )
            }
        }
    }
}