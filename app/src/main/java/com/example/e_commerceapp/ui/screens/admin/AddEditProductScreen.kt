package com.example.e_commerceapp.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String? = null,
    viewModel: AdminViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val opStatus by viewModel.opStatus.collectAsStateWithLifecycle()

    val isEditing = !productId.isNullOrBlank()
    val existingProduct = remember(productId, products) {
        products.find { it.id == productId }
    }

    var title by remember { mutableStateOf(existingProduct?.title ?: "") }
    var description by remember { mutableStateOf(existingProduct?.description ?: "") }
    var priceStr by remember { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var mrpPriceStr by remember { mutableStateOf(existingProduct?.mrpPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(existingProduct?.stockQuantity?.toString() ?: "20") }
    var brand by remember { mutableStateOf(existingProduct?.brand ?: "") }
    var categoryId by remember { mutableStateOf(existingProduct?.categoryId ?: "") }
    var imageUrl by remember { mutableStateOf(existingProduct?.imageUrl ?: "") }
    var deliveryDaysStr by remember { mutableStateOf(existingProduct?.deliveryDays?.toString() ?: "2") }
    var isCodAvailable by remember { mutableStateOf(existingProduct?.isCodAvailable ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingProduct) {
        existingProduct?.let {
            title = it.title
            description = it.description
            priceStr = it.price.toString()
            mrpPriceStr = it.mrpPrice.toString()
            stockStr = it.stockQuantity.toString()
            brand = it.brand
            categoryId = it.categoryId
            imageUrl = it.imageUrl.ifBlank { it.images.firstOrNull() ?: "" }
            deliveryDaysStr = it.deliveryDays.toString()
            isCodAvailable = it.isCodAvailable
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Product" else "Add New Product",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0B132B)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Image Preview Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Preview",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Image Live Preview",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (imageUrl.isBlank()) "Enter a valid Image URL below" else "Previewing remote image",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            AdminInputField(
                label = "Product Title *",
                value = title,
                onValueChange = { title = it }
            )

            AdminInputField(
                label = "Image URL *",
                value = imageUrl,
                onValueChange = { imageUrl = it }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminInputField(
                    modifier = Modifier.weight(1f),
                    label = "Brand",
                    value = brand,
                    onValueChange = { brand = it }
                )
                AdminInputField(
                    modifier = Modifier.weight(1f),
                    label = "Category ID/Slug",
                    value = categoryId,
                    onValueChange = { categoryId = it }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminInputField(
                    modifier = Modifier.weight(1f),
                    label = "Selling Price (₹) *",
                    value = priceStr,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = {
                        priceStr = it
                        // Auto-calculate suggested MRP if empty
                        val priceNum = it.toDoubleOrNull() ?: 0.0
                        if (mrpPriceStr.isBlank() && priceNum > 0) {
                            mrpPriceStr = (priceNum * 1.35).toInt().toString()
                        }
                    }
                )
                AdminInputField(
                    modifier = Modifier.weight(1f),
                    label = "MRP Price (₹) *",
                    value = mrpPriceStr,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = { mrpPriceStr = it }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminInputField(
                    modifier = Modifier.weight(1f),
                    label = "Stock Quantity *",
                    value = stockStr,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { stockStr = it }
                )
                AdminInputField(
                    modifier = Modifier.weight(1f),
                    label = "Delivery Days",
                    value = deliveryDaysStr,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { deliveryDaysStr = it }
                )
            }

            AdminInputField(
                label = "Product Description",
                value = description,
                singleLine = false,
                onValueChange = { description = it }
            )

            // COD Available Switch Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cash on Delivery (COD) Available",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Allows Indian customers to pay on delivery",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isCodAvailable,
                    onCheckedChange = { isCodAvailable = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                    )
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Product Button
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull()
                    val mrp = mrpPriceStr.toDoubleOrNull() ?: (price?.times(1.35) ?: 0.0)
                    val stock = stockStr.toIntOrNull() ?: 0
                    val deliveryDays = deliveryDaysStr.toIntOrNull() ?: 2

                    if (title.isBlank()) {
                        errorMessage = "Product title is required"
                        return@Button
                    }
                    if (price == null || price <= 0) {
                        errorMessage = "Valid selling price in ₹ is required"
                        return@Button
                    }
                    if (mrp < price) {
                        errorMessage = "MRP (₹) cannot be less than selling price (₹)"
                        return@Button
                    }

                    errorMessage = null
                    val resolvedId = if (isEditing && !productId.isNullOrBlank()) {
                        productId
                    } else {
                        "prod_${UUID.randomUUID().toString().take(8)}"
                    }

                    val productToSave = FirestoreProductDto(
                        id = resolvedId,
                        title = title.trim(),
                        description = description.trim(),
                        price = price,
                        mrpPrice = mrp,
                        discountPercentage = if (mrp > price) (((mrp - price) / mrp) * 100) else 0.0,
                        categoryId = categoryId.trim().lowercase(),
                        categoryName = categoryId.trim().replaceFirstChar { it.uppercase() },
                        images = listOf(imageUrl.trim()).filter { it.isNotBlank() },
                        imageUrl = imageUrl.trim(),
                        stockQuantity = stock,
                        inStock = stock > 0,
                        rating = existingProduct?.rating ?: 4.5,
                        reviewsCount = existingProduct?.reviewsCount ?: 12,
                        brand = brand.trim().ifBlank { "Generic" },
                        deliveryDays = deliveryDays,
                        isCodAvailable = isCodAvailable,
                        updatedAt = System.currentTimeMillis(),
                        createdAt = existingProduct?.createdAt ?: System.currentTimeMillis()
                    )

                    viewModel.saveProduct(productToSave) {
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                enabled = opStatus !is AdminOpStatus.Loading
            ) {
                if (opStatus is AdminOpStatus.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Update Product" else "Create Product",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AdminInputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B),
            )
        )
    }
}
