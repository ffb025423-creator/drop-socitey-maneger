package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.ReportsData
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
    reportsData: ReportsData,
    expenses: List<ExpenseEntity>,
    currencySymbol: String,
    onAddExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Analytics") }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Tabs ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REPORTS & EXPENSES",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Export Reports Button
            IconButton(
                onClick = {
                    shareCsvReport(context, reportsData, expenses, currencySymbol)
                },
                modifier = Modifier
                    .background(GlassWhite, RoundedCornerShape(8.dp))
                    .size(36.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Export Report", tint = PrimaryRed)
            }
        }

        // --- Custom Toggle Tab ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == "Analytics") PrimaryRed else Color.Transparent)
                    .clickable { selectedTab = "Analytics" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Analytics Summary", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == "Expenses") PrimaryRed else Color.Transparent)
                    .clickable { selectedTab = "Expenses" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Expense Book", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
        }

        if (selectedTab == "Analytics") {
            // --- ANALYTICS TAB ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // KPIs grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ReportsKpiCard(
                                title = "Gross Profit",
                                value = String.format("%s%,.2f", currencySymbol, reportsData.grossProfit),
                                color = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            ReportsKpiCard(
                                title = "Net Profit",
                                value = String.format("%s%,.2f", currencySymbol, reportsData.netProfit),
                                color = if (reportsData.netProfit >= 0) Color.White else ErrorRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ReportsKpiCard(
                                title = "Daily Profit",
                                value = String.format("%s%,.2f", currencySymbol, reportsData.dailyProfit),
                                color = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            ReportsKpiCard(
                                title = "Monthly Profit",
                                value = String.format("%s%,.2f", currencySymbol, reportsData.monthlyProfit),
                                color = Color.Cyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Visual bar chart for Top Selling Products
                if (reportsData.topSellingProducts.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Top Selling Products", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                BarChart(items = reportsData.topSellingProducts, color = PrimaryRed)
                            }
                        }
                    }
                }

                // Top Sizes and Categories distribution list
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Top Category Allocations", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                            if (reportsData.topCategories.isEmpty()) {
                                Text("No sales data recorded yet.", color = TextGray, fontSize = 12.sp)
                            } else {
                                reportsData.topCategories.forEach { (cat, qty) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cat, color = Color.White, fontSize = 13.sp)
                                        Text("$qty Units", color = PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Most Ordered Sizes", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                            if (reportsData.topSizes.isEmpty()) {
                                Text("No size data recorded.", color = TextGray, fontSize = 12.sp)
                            } else {
                                reportsData.topSizes.forEach { (sz, qty) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Size $sz", color = Color.White, fontSize = 13.sp)
                                        Text("$qty Orders", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- EXPENSES TAB ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total expenses header display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryRed.copy(alpha = 0.15f))
                        .border(1.dp, PrimaryRed.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL ACCUMULATED EXPENSES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Text(
                                text = String.format("%s%,.2f", currencySymbol, reportsData.totalExpenses),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { showExpenseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add Expense", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Expenses list
                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No expenses logged today.", color = TextGray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(expenses) { exp ->
                            ExpenseItemCard(
                                expense = exp,
                                currencySymbol = currencySymbol,
                                onDelete = { expenseToDelete = exp }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Add Expense Dialog ---
    if (showExpenseDialog) {
        ExpenseFormDialog(
            onDismiss = { showExpenseDialog = false },
            onSave = {
                onAddExpense(it)
                showExpenseDialog = false
            }
        )
    }

    // --- Delete Expense Dialog ---
    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = CardBackground,
            title = { Text("Delete Expense Record?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete expense for ${expenseToDelete!!.category} costing ${currencySymbol}${expenseToDelete!!.amount}?", color = TextGray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteExpense(expenseToDelete!!)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ReportsKpiCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, GlassWhite, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BarChart(
    items: List<Pair<String, Int>>,
    color: Color
) {
    val maxVal = (items.maxOfOrNull { it.second } ?: 1).toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (label, count) ->
            val fraction = count.toFloat() / maxVal
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("$count Pcs", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    // Gray track
                    drawRoundRect(
                        color = Color(0xFF2C2C2C),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                    // Colored progress
                    drawRoundRect(
                        color = color,
                        size = Size(size.width * fraction, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, GlassWhite, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                if (expense.notes.isNotEmpty()) {
                    Text(expense.notes, color = TextGray, fontSize = 12.sp)
                }
                Text(
                    text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(expense.date)),
                    color = TextGray,
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "-${currencySymbol}${expense.amount}",
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExpenseFormDialog(
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Packaging") }

    val categories = listOf("Shop Rent", "Packaging", "Advertising", "Courier Cost", "Transport", "Internet", "Electricity", "Employee Salary", "Miscellaneous")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Log New Expense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight(0.65f)
            ) {
                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Expense Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("form_expense_amount"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                item {
                    Text("Select Category", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (category == cat) PrimaryRed.copy(alpha = 0.25f) else Color(0xFF222222))
                                    .clickable { category = cat }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat, color = Color.White, fontSize = 13.sp)
                                if (category == cat) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = PrimaryRed)
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Description") },
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
                    val parsedAmt = amount.toDoubleOrNull() ?: 0.0
                    if (parsedAmt > 0) {
                        onSave(
                            ExpenseEntity(
                                category = category,
                                amount = parsedAmt,
                                notes = notes,
                                date = System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

// Share reports using action intent share
fun shareCsvReport(context: Context, reportsData: ReportsData, expenses: List<ExpenseEntity>, currency: String) {
    try {
        val reportBuilder = StringBuilder()
        reportBuilder.append("DROP SOCIETY BUSINESS ANALYTICS REPORT\n")
        reportBuilder.append("Generated on: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n\n")
        reportBuilder.append("METRICS SUMMARY:\n")
        reportBuilder.append("Gross Profit: $currency${reportsData.grossProfit}\n")
        reportBuilder.append("Total Expenses: $currency${reportsData.totalExpenses}\n")
        reportBuilder.append("Net Profit: $currency${reportsData.netProfit}\n")
        reportBuilder.append("Total Revenue: $currency${reportsData.totalRevenue}\n")
        reportBuilder.append("Discounts Granted: $currency${reportsData.totalDiscount}\n")
        reportBuilder.append("Courier/Delivery Income: $currency${reportsData.totalDeliveryCharges}\n")
        reportBuilder.append("Delivered Orders Count: ${reportsData.deliveredOrdersCount}\n")
        reportBuilder.append("Cancelled Orders Count: ${reportsData.cancelledOrdersCount}\n\n")

        reportBuilder.append("EXPENSES DETAILS:\n")
        reportBuilder.append("Category, Amount, Date, Notes\n")
        expenses.forEach { exp ->
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(exp.date))
            reportBuilder.append("${exp.category}, ${exp.amount}, $df, ${exp.notes.replace(",", " ")}\n")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DROP SOCIETY Analytics Report")
            putExtra(Intent.EXTRA_TEXT, reportBuilder.toString())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Report via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Exporting failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
