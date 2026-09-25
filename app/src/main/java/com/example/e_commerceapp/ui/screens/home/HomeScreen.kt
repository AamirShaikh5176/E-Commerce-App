package com.example.e_commerceapp.ui.screens.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.navigation.bottomNavigation.AppBottomNavBar
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.home.component.CategoryItem
import com.example.e_commerceapp.ui.screens.home.component.HeaderHomeScreen
import com.example.e_commerceapp.ui.screens.home.component.ProductItem
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid as LazyVerticalGrid1

@Composable
fun HomeScreen(
    onProductClicked: (ProductEntity) -> Unit,
    onNotificationClick: () -> Unit,
    onClickSearchBar: () -> Unit,
    onSeeAllCategoryClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeContent(
        modifier = Modifier,
        onProductClicked = onProductClicked,
        onNotificationClick = onNotificationClick,
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
    onSeeAllCategoryClick: () -> Unit,
    onClickSearchBar: () -> Unit,
    viewModel: HomeViewModel,
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val categoryState by viewModel.categoryState.collectAsStateWithLifecycle()
    val productState by viewModel.productState.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    HideNavigationBarOnly()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(16.dp)
    ) {


        HeaderHomeScreen(
            userInformationEntity = userState.data,
            onNotificationClick = onNotificationClick,
            onClickSearchBar = onClickSearchBar
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------- Offers Carousel --------------------

        Text(
            text = "Special Offers",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.cairo_regular))
        )

        Spacer(modifier = Modifier.height(4.dp))

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
                delay(3000)
                val nextPage = (pagerState.currentPage + 1) % offers.size
                pagerState.animateScrollToPage(nextPage)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = offers[page]),
                    contentDescription = "Offer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------- Categories Horizontal Scroll --------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Categories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.cairo_regular))
            )

            Text(
                text = "See All",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.cairo_regular)),
                modifier = Modifier.clickable { onSeeAllCategoryClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LaunchedEffect(categoryState.data) {
            categoryState.data?.firstOrNull()?.let { firstCategory ->
                selectedCategory = firstCategory.name
                viewModel.getProductsByCategory(firstCategory.name)
            }
        }

        if (categoryState.data != null) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------- Top Products Grid --------------------

        when {
            productState.isLoading -> {
                Loading()
            }

            productState.data != null -> {
                LazyVerticalGrid1(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 600.dp)
                ) {
                    items(productState.data?.take(4) ?: emptyList()) { productEntity ->
                        ProductItem(
                            product = productEntity,
                            onProductClicked = onProductClicked
                        )
                    }
                }
            }

            productState.errorMessage != null -> {
                ErrorMessage(productState.errorMessage.toString())
            }
        }
    }
}