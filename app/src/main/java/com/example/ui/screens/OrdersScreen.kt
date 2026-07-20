package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    currencySymbol: String,
    onSaveOrder: (OrderEntity) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onDeleteOrder: (OrderEntity) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusTab by remember { mutableStateOf("All") }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var viewingInvoice by remember { mutableStateOf<OrderEntity?>(null) }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }

    val statusTabs = listOf("All", "Pending", "Processing", "Shipped", "Delivered", "Cancelled")

    // Filter orders
    val filteredOrders = orders.filter { o ->
        val matchesSearch = o.customerName.contains(searchQuery, ignoreCase = true) ||
                o.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                o.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                o.productName.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusTab == "All" || o.status.equals(selectedStatusTab, ignoreCase = true)
        matchesSearch && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (products.isEmpty()) {
                        Toast.makeText(context, "Please add a product first before creating an order.", Toast.LENGTH_SHORT).show()
                    } else {
                        showAddDialog = true
                    }
                },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("create_order_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Order")
            }
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("orders_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ORDER MANAGER",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // --- Search Input ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_search_input"),
                placeholder = { Text("Search by name, phone, invoice, or product...", color = TextGray) },
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

            // --- Status filter tabs ---
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusTabs) { status ->
                    val isSelected = selectedStatusTab == status
                    val tabColor = when (status) {
                        "Pending" -> WarningYellow
                        "Processing" -> Color.Cyan
                        "Shipped" -> Color.Blue
                        "Delivered" -> SuccessGreen
                        "Cancelled" -> ErrorRed
                        else -> PrimaryRed
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) tabColor else CardBackground
                            )
                            .clickable { selectedStatusTab = status }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextGray
                        )
                    }
                }
            }

            // --- Orders List ---
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Empty",
                            tint = TextGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No orders recorded yet.", color = TextGray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredOrders) { ord ->
                        OrderItemCard(
                            order = ord,
                            currencySymbol = currencySymbol,
                            onEdit = { editingOrder = ord },
                            onDelete = { orderToDelete = ord },
                            onViewInvoice = { viewingInvoice = ord },
                            onQuickStatusChange = { newStat -> onUpdateStatus(ord.id, newStat) }
                        )
                    }
                }
            }
        }
    }

    // --- Create/Edit Order Dialogs ---
    if (showAddDialog) {
        OrderFormDialog(
            order = null,
            products = products,
            onDismiss = { showAddDialog = false },
            onSave = {
                onSaveOrder(it)
                showAddDialog = false
            }
        )
    }

    if (editingOrder != null) {
        OrderFormDialog(
            order = editingOrder,
            products = products,
            onDismiss = { editingOrder = null },
            onSave = {
                onSaveOrder(it)
                editingOrder = null
            }
        )
    }

    // --- Invoice Dialog ---
    if (viewingInvoice != null) {
        InvoiceDialog(
            order = viewingInvoice!!,
            currencySymbol = currencySymbol,
            onDismiss = { viewingInvoice = null }
        )
    }

    // --- Delete Dialog ---
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            containerColor = CardBackground,
            title = { Text("Delete Order?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete order #${orderToDelete!!.invoiceNumber}?", color = TextGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteOrder(orderToDelete!!)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun OrderItemCard(
    order: OrderEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewInvoice: () -> Unit,
    onQuickStatusChange: (String) -> Unit
) {
    val context = LocalContext.current
    val grandTotal = (order.sellingPrice - order.discount) * order.quantity + order.deliveryCharge

    val statusColor = when (order.status) {
        "Pending" -> WarningYellow
        "Processing" -> Color.Cyan
        "Shipped" -> Color.Blue
        "Delivered" -> SuccessGreen
        "Cancelled" -> ErrorRed
        else -> TextGray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INV: ${order.invoiceNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.orderDate)),
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = GlassWhite, thickness = 1.dp)

            // Customer Details
            Column {
                Text(text = order.customerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Text(text = "Phone: ${order.phoneNumber}", color = TextGray, fontSize = 13.sp)
                Text(text = "Address: ${order.address}", color = TextGray, fontSize = 13.sp, maxLines = 1)
            }

            // Products list line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassWhite)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${order.productName} (${order.size} / ${order.color})",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Qty: ${order.quantity}",
                    color = PrimaryRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Totals and grand total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Discount: ${currencySymbol}${order.discount}", fontSize = 11.sp, color = TextGray)
                    Text(text = "Advance: ${currencySymbol}${order.advancePayment}", fontSize = 11.sp, color = TextGray)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GRAND TOTAL", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%s%,.2f", currencySymbol, grandTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            HorizontalDivider(color = GlassWhite, thickness = 1.dp)

            // Bottom Action Bar (Intents, Status, and Edit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Communication buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${order.phoneNumber}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dialer app not found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E3A1E), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            try {
                                val message = "Hello ${order.customerName},\nThis is DROP SOCIETY. Your order #${order.invoiceNumber} for ${order.productName} is currently ${order.status}.\nGrand Total: ${currencySymbol}${grandTotal}.\nThank you!"
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=${order.phoneNumber}&text=${Uri.encode(message)}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0F3124), CircleShape)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onViewInvoice,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GlassWhite, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = "Invoice", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }

                // Action controls: Quick Deliver / Cancel or Edit dropdown
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (order.status != "Delivered" && order.status != "Cancelled") {
                        TextButton(
                            onClick = { onQuickStatusChange("Delivered") },
                            colors = ButtonDefaults.textButtonColors(contentColor = SuccessGreen)
                        ) {
                            Text("Deliver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { onQuickStatusChange("Cancelled") },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                        ) {
                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .background(GlassWhite, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(GlassWhite, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormDialog(
    order: OrderEntity?,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSave: (OrderEntity) -> Unit
) {
    var customerName by remember { mutableStateOf(order?.customerName ?: "") }
    var phoneNumber by remember { mutableStateOf(order?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(order?.address ?: "") }
    var courier by remember { mutableStateOf(order?.courier ?: "Steadfast") }
    
    // Choose product
    var selectedProduct by remember { mutableStateOf(products.firstOrNull { it.id == order?.productId } ?: products.first()) }
    var size by remember { mutableStateOf(order?.size ?: selectedProduct.sizes.firstOrNull() ?: "M") }
    var color by remember { mutableStateOf(order?.color ?: selectedProduct.colors.firstOrNull() ?: "Black") }
    var quantity by remember { mutableStateOf(order?.quantity?.toString() ?: "1") }
    var sellingPrice by remember { mutableStateOf(order?.sellingPrice?.toString() ?: selectedProduct.sellingPrice.toString()) }
    
    var discount by remember { mutableStateOf(order?.discount?.toString() ?: "0") }
    var deliveryCharge by remember { mutableStateOf(order?.deliveryCharge?.toString() ?: "120") }
    var advancePayment by remember { mutableStateOf(order?.advancePayment?.toString() ?: "0") }
    var paymentStatus by remember { mutableStateOf(order?.paymentStatus ?: "Unpaid") }
    var notes by remember { mutableStateOf(order?.notes ?: "") }
    var status by remember { mutableStateOf(order?.status ?: "Pending") }

    val couriers = listOf("Steadfast", "Pathao", "RedX", "Paperfly")
    val statuses = listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled")

    // Update prices when product switches
    LaunchedEffect(selectedProduct) {
        if (order == null) {
            sellingPrice = selectedProduct.sellingPrice.toString()
            size = selectedProduct.sizes.firstOrNull() ?: "M"
            color = selectedProduct.colors.firstOrNull() ?: "Black"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = if (order == null) "CREATE NEW ORDER" else "EDIT ORDER",
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
                // Customer details
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("form_customer_name"),
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
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Product selector
                item {
                    Text("Select Product", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        products.take(6).forEach { prod ->
                            val isSelected = selectedProduct.id == prod.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryRed.copy(alpha = 0.25f) else Color(0xFF222222))
                                    .border(1.dp, if (isSelected) PrimaryRed else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedProduct = prod }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(prod.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("SKU: ${prod.sku} | Avail Stock: ${prod.stockQuantity} Pcs", color = TextGray, fontSize = 11.sp)
                                }
                                Text("${prod.sellingPrice}", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Size & Color options
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Size", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val sizes = selectedProduct.sizes.ifEmpty { listOf("S", "M", "L", "XL") }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                sizes.forEach { s ->
                                    val isSelected = size == s
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) PrimaryRed else Color(0xFF2C2C2C))
                                            .clickable { size = s }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(s, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Color", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val colors = selectedProduct.colors.ifEmpty { listOf("Black", "White", "Navy") }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                colors.forEach { c ->
                                    val isSelected = color == c
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) PrimaryRed else Color(0xFF2C2C2C))
                                            .clickable { color = c }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(c, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Quantity and custom pricing
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") },
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
                            label = { Text("Unit Price") },
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

                // Billing inputs
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            label = { Text("Discount/Pc") },
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
                            value = deliveryCharge,
                            onValueChange = { deliveryCharge = it },
                            label = { Text("Delivery Charge") },
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
                        value = advancePayment,
                        onValueChange = { advancePayment = it },
                        label = { Text("Advance Payment") },
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

                // Courier selection
                item {
                    Text("Courier", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        couriers.forEach { cour ->
                            val isSelected = courier == cour
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryRed else Color(0xFF2C2C2C))
                                    .clickable { courier = cour }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cour, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Order Status
                item {
                    Text("Order Status", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        statuses.forEach { stat ->
                            val isSelected = status == stat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryRed else Color(0xFF2C2C2C))
                                    .clickable { status = stat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(stat, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Order Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotEmpty() && phoneNumber.isNotEmpty() && address.isNotEmpty()) {
                        val invoiceNum = order?.invoiceNumber ?: "DS-${System.currentTimeMillis() % 1000000}"
                        onSave(
                            OrderEntity(
                                id = order?.id ?: 0L,
                                invoiceNumber = invoiceNum,
                                customerName = customerName,
                                phoneNumber = phoneNumber,
                                address = address,
                                courier = courier,
                                productId = selectedProduct.id,
                                productName = selectedProduct.name,
                                size = size,
                                color = color,
                                quantity = quantity.toIntOrNull() ?: 1,
                                sellingPrice = sellingPrice.toDoubleOrNull() ?: selectedProduct.sellingPrice,
                                buyingPrice = selectedProduct.buyingPrice, // Log buying price at order time
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                deliveryCharge = deliveryCharge.toDoubleOrNull() ?: 120.0,
                                advancePayment = advancePayment.toDoubleOrNull() ?: 0.0,
                                paymentStatus = if ((advancePayment.toDoubleOrNull() ?: 0.0) >= ((sellingPrice.toDoubleOrNull() ?: 0.0) * (quantity.toIntOrNull() ?: 1))) "Paid" else if ((advancePayment.toDoubleOrNull() ?: 0.0) > 0) "Partially Paid" else "Unpaid",
                                notes = notes,
                                status = status,
                                orderDate = order?.orderDate ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold, color = Color.White)
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
fun InvoiceDialog(
    order: OrderEntity,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    val grandTotal = (order.sellingPrice - order.discount) * order.quantity + order.deliveryCharge

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White, // White traditional receipt background
        title = {
            Text(
                "DROP SOCIETY RETAIL RECEIPT",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Invoice No: ${order.invoiceNumber}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.orderDate))}", color = Color.Black, fontSize = 11.sp)
                
                HorizontalDivider(color = Color.Black, thickness = 1.dp)

                Text("BILL TO:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Name: ${order.customerName}", color = Color.Black, fontSize = 11.sp)
                Text("Phone: ${order.phoneNumber}", color = Color.Black, fontSize = 11.sp)
                Text("Address: ${order.address}", color = Color.Black, fontSize = 11.sp)

                HorizontalDivider(color = Color.Black, thickness = 1.dp)

                // Item description
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Item", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                    Text("Qty", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("Price", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${order.productName}\nSize: ${order.size} | Color: ${order.color}", color = Color.Black, fontSize = 11.sp, modifier = Modifier.weight(2f))
                    Text(order.quantity.toString(), color = Color.Black, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(String.format("%s%,.0f", currencySymbol, order.sellingPrice), color = Color.Black, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = Color.Black, thickness = 0.5.dp)

                // Sums
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = Color.Black, fontSize = 11.sp)
                    Text("${currencySymbol}${order.sellingPrice * order.quantity}", color = Color.Black, fontSize = 11.sp)
                }
                if (order.discount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", color = Color.Black, fontSize = 11.sp)
                        Text("-${currencySymbol}${order.discount * order.quantity}", color = Color.Black, fontSize = 11.sp)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery Charge", color = Color.Black, fontSize = 11.sp)
                    Text("+${currencySymbol}${order.deliveryCharge}", color = Color.Black, fontSize = 11.sp)
                }

                HorizontalDivider(color = Color.Black, thickness = 1.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GRAND TOTAL", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(String.format("%s%,.2f", currencySymbol, grandTotal), color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Courier: ${order.courier}", color = Color.Black, fontSize = 11.sp)
                    Text("Status: ${order.status}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                HorizontalDivider(color = Color.Black, thickness = 0.5.dp)

                Text(
                    text = "Thank you for shopping with DROP SOCIETY!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("Dismiss", color = Color.White)
            }
        }
    )
}
