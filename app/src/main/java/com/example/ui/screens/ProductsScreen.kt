package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.theme.*

@Composable
fun ProductsScreen(
    products: List<ProductEntity>,
    currencySymbol: String,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var viewingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf("All", "Jersey", "Drop Shoulder", "Polo Shirt", "Hoodie", "Pant")

    // Filtered Products
    val filteredProducts = products.filter { p ->
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("products_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Elegant Search & Header ---
            Text(
                text = "INVENTORY MANAGEMENT",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input"),
                placeholder = { Text("Search by name, SKU, or barcode...", color = TextGray) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PrimaryRed) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRed,
                    unfocusedBorderColor = GlassWhite,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // --- Horizontal Categories List ---
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) PrimaryRed else CardBackground
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextGray
                        )
                    }
                }
            }

            // --- Products List ---
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Empty",
                            tint = TextGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products found in inventory.", color = TextGray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts) { prod ->
                        ProductItemCard(
                            product = prod,
                            currencySymbol = currencySymbol,
                            onEdit = { editingProduct = prod },
                            onDelete = { productToDelete = prod },
                            onView = { viewingProduct = prod }
                        )
                    }
                }
            }
        }
    }

    // --- Add/Edit Dialogs ---
    if (showAddDialog) {
        ProductFormDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = {
                onSaveProduct(it)
                showAddDialog = false
            }
        )
    }

    if (editingProduct != null) {
        ProductFormDialog(
            product = editingProduct,
            onDismiss = { editingProduct = null },
            onSave = {
                onSaveProduct(it)
                editingProduct = null
            }
        )
    }

    // --- Detail Sheet bottom dialog ---
    if (viewingProduct != null) {
        ProductDetailDialog(
            product = viewingProduct!!,
            currencySymbol = currencySymbol,
            onDismiss = { viewingProduct = null }
        )
    }

    // --- Delete Confirmation Dialog ---
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = CardBackground,
            title = { Text("Delete Product?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${productToDelete!!.name}? This action cannot be undone.", color = TextGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(productToDelete!!.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    currencySymbol: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
            .clickable { onView() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.category,
                        color = PrimaryRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (product.status == "Active") SuccessGreen.copy(alpha = 0.15f) else Color.DarkGray)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.status,
                            color = if (product.status == "Active") SuccessGreen else Color.LightGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = product.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "SKU: ${product.sku} | Stock: ${product.stockQuantity} Pcs",
                    color = if (product.stockQuantity <= product.lowStockThreshold) ErrorRed else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%s%,.0f", currencySymbol, product.sellingPrice),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cost: ${currencySymbol}${product.buyingPrice}",
                    color = TextGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .background(GlassWhite, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(GlassWhite, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Jersey") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var buyingPrice by remember { mutableStateOf(product?.buyingPrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "") }
    var sizesInput by remember { mutableStateOf(product?.sizes?.joinToString(", ") ?: "S, M, L, XL") }
    var colorsInput by remember { mutableStateOf(product?.colors?.joinToString(", ") ?: "Black, White") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var status by remember { mutableStateOf(product?.status ?: "Active") }
    var imageInput by remember { mutableStateOf(product?.images?.firstOrNull() ?: "") }

    val categories = listOf("Jersey", "Drop Shoulder", "Polo Shirt", "Hoodie", "Pant")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = if (product == null) "ADD PRODUCT" else "EDIT PRODUCT",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight(0.75f)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth().testTag("form_product_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Text("Category", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            val isSelected = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryRed else Color(0xFF2C2C2C))
                                    .clickable { category = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Code") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyingPrice,
                            onValueChange = { buyingPrice = it },
                            label = { Text("Buying Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRed,
                                unfocusedBorderColor = GlassWhite,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = { Text("Selling Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRed,
                                unfocusedBorderColor = GlassWhite,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = sizesInput,
                        onValueChange = { sizesInput = it },
                        label = { Text("Sizes (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = colorsInput,
                        onValueChange = { colorsInput = it },
                        label = { Text("Colors (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / QR String") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = imageInput,
                        onValueChange = { imageInput = it },
                        label = { Text("Image URL Path (Optional)") },
                        placeholder = { Text("https://example.com/jersey.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Active", color = Color.White)
                        Switch(
                            checked = status == "Active",
                            onCheckedChange = { status = if (it) "Active" else "Inactive" },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryRed, checkedTrackColor = PrimaryRed.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && sku.isNotEmpty()) {
                        val parsedSizes = sizesInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val parsedColors = colorsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val imagesList = if (imageInput.trim().isNotEmpty()) listOf(imageInput.trim()) else emptyList()

                        onSave(
                            ProductEntity(
                                id = product?.id ?: 0L,
                                name = name,
                                category = category,
                                sku = sku,
                                buyingPrice = buyingPrice.toDoubleOrNull() ?: 0.0,
                                sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                stockQuantity = stockQuantity.toIntOrNull() ?: 0,
                                sizes = parsedSizes,
                                colors = parsedColors,
                                images = imagesList,
                                barcode = barcode.ifEmpty { "DS-${sku}" },
                                qrCode = barcode.ifEmpty { "DS-${sku}" },
                                description = description,
                                status = status,
                                timestamp = product?.timestamp ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun ProductDetailDialog(
    product: ProductEntity,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text(product.name, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SKU: ${product.sku}", color = TextGray)
                Text("Category: ${product.category}", color = PrimaryRed, fontWeight = FontWeight.Bold)
                Text("Buying Cost: ${currencySymbol}${product.buyingPrice}", color = Color.White)
                Text("Selling Cost: ${currencySymbol}${product.sellingPrice}", color = SuccessGreen, fontWeight = FontWeight.Bold)
                Text("Sizes: ${product.sizes.joinToString(", ")}", color = Color.White)
                Text("Colors: ${product.colors.joinToString(", ")}", color = Color.White)
                Text("Stock Balance: ${product.stockQuantity} Pcs", color = if (product.stockQuantity < 5) ErrorRed else Color.White, fontWeight = FontWeight.Bold)
                if (product.description.isNotEmpty()) {
                    Text("Description: ${product.description}", color = TextGray)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Barcode/QR visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(160.dp, 60.dp)) {
                            // Render mockup clean bar codes
                            val linesCount = 28
                            val spacing = this.size.width / linesCount
                            for (i in 0 until linesCount) {
                                val strokeW = if (i % 3 == 0) 4f else if (i % 5 == 0) 8f else 2f
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(i * spacing, 0f),
                                    end = Offset(i * spacing, this.size.height),
                                    strokeWidth = strokeW
                                )
                            }
                        }
                        Text(
                            text = product.barcode,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)) {
                Text("Dismiss", color = Color.White)
            }
        }
    )
}
