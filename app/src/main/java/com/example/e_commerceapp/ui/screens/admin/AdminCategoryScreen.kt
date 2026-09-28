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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val opStatus by viewModel.opStatus.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<FirestoreCategoryDto?>(null) }
    var categoryToEdit by remember { mutableStateOf<FirestoreCategoryDto?>(null) }

    var newCategoryName by remember { mutableStateOf("") }
    var newCategorySlug by remember { mutableStateOf("") }

    var editCategoryName by remember { mutableStateOf("") }
    var editCategorySlug by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Category", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminInputField(
                        label = "Category Name",
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                            if (newCategorySlug.isBlank()) {
                                newCategorySlug = it.trim().lowercase().replace(" ", "-")
                            }
                        }
                    )
                    AdminInputField(
                        label = "Slug / ID",
                        value = newCategorySlug,
                        onValueChange = { newCategorySlug = it }
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank() && newCategorySlug.isNotBlank()) {
                            viewModel.addCategory(
                                FirestoreCategoryDto(
                                    id = newCategorySlug.trim(),
                                    name = newCategoryName.trim(),
                                    slug = newCategorySlug.trim(),
                                    url = ""
                                )
                            ) {
                                newCategoryName = ""
                                newCategorySlug = ""
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("Add", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Remove Category?", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete category '${categoryToDelete?.name}'?",
                    color = Color(0xFF94A3B8)
                )
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it.slug.ifBlank { it.id }) }
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (categoryToEdit != null) {
        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = { Text("Edit Category", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminInputField(
                        label = "Category Name",
                        value = editCategoryName,
                        onValueChange = { editCategoryName = it }
                    )
                    AdminInputField(
                        label = "Slug / ID",
                        value = editCategorySlug,
                        onValueChange = { editCategorySlug = it }
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                TextButton(
                    onClick = {
                        val current = categoryToEdit ?: return@TextButton
                        if (editCategoryName.isNotBlank() && editCategorySlug.isNotBlank()) {
                            viewModel.addCategory(
                                current.copy(
                                    name = editCategoryName.trim(),
                                    slug = editCategorySlug.trim(),
                                    id = current.id.ifBlank { editCategorySlug.trim() }
                                )
                            ) {
                                categoryToEdit = null
                            }
                        }
                    }
                ) {
                    Text("Save", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEdit = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Categories (${categories.size})",
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
                actions = {
                    AdminSyncButton(
                        opStatus = opStatus,
                        onSyncClick = { viewModel.syncCatalog() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = Color(0xFF0B132B)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (categories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Categories Found",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sync categories from catalog to populate automatically.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.syncCatalog() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Catalog Categories")
                            }
                        }
                    }
                }
            }

            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFA855F7).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.name.ifBlank { category.slug },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "slug: ${category.slug}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = {
                            categoryToEdit = category
                            editCategoryName = category.name
                            editCategorySlug = category.slug
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = { categoryToDelete = category }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        AdminSyncConfirmationBanner(
            opStatus = opStatus,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
}
