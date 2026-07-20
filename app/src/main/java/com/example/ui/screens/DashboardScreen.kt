package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardData
import com.example.data.model.ProductEntity
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    dashboardData: DashboardData,
    currencySymbol: String,
    onNavigateToTab: (Int) -> Unit
) {
    var animateTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    // Profit counting-up animation
    val animatedTodayProfit by animateFloatAsState(
        targetValue = if (animateTrigger) dashboardData.todayProfit.toFloat() else 0f,
        animationSpec = tween(1500, easing = EaseOutQuad),
        label = "todayProfitAnim"
    )

    val animatedMonthlyProfit by animateFloatAsState(
        targetValue = if (animateTrigger) dashboardData.monthlyProfit.toFloat() else 0f,
        animationSpec = tween(1500, easing = EaseOutQuad),
        label = "monthlyProfitAnim"
    )

    val animatedRevenue by animateFloatAsState(
        targetValue = if (animateTrigger) dashboardData.totalRevenue.toFloat() else 0f,
        animationSpec = tween(1500, easing = EaseOutQuad),
        label = "revenueAnim"
    )

    val animatedTodaySales by animateFloatAsState(
        targetValue = if (animateTrigger) dashboardData.todaySales.toFloat() else 0f,
        animationSpec = tween(1500, easing = EaseOutQuad),
        label = "todaySalesAnim"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Hero Profit Card (Total Revenue & Profits Split) ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF181818),
                                Color(0xFF111111)
                            )
                        )
                    )
                    .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                // Subtle top-right red background glow
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryRed.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Column {
                    Text(
                        text = "TOTAL REVENUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format("%s%,.0f", currencySymbol, animatedRevenue),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Trend up",
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "12.5%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0x0DFFFFFF), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TODAY'S SALES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("%s%,.0f", currencySymbol, animatedTodaySales),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TODAY'S PROFIT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("%s%,.0f", currencySymbol, animatedTodayProfit),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                        }

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "MONTHLY PROFIT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.4f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format("%s%,.0f", currencySymbol, animatedMonthlyProfit),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- Core Metric Stats Grid (2x2) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Pending Orders",
                        value = dashboardData.pendingOrdersCount.toString(),
                        icon = Icons.Default.ShoppingBag,
                        tint = PrimaryRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(2) }
                    )
                    MetricCard(
                        title = "Delivered",
                        value = dashboardData.deliveredOrdersCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        tint = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(2) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Low Stock",
                        value = dashboardData.lowStockProductsCount.toString(),
                        icon = Icons.Default.Inventory,
                        tint = WarningYellow,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(1) }
                    )
                    MetricCard(
                        title = "Cancelled",
                        value = dashboardData.cancelledOrdersCount.toString(),
                        icon = Icons.Default.Cancel,
                        tint = ErrorRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(2) }
                    )
                }
            }
        }

        // --- Quick Alert Pulsing Section ---
        item {
            QuickAlertSection(
                pendingCount = dashboardData.pendingOrdersCount,
                lowStockCount = dashboardData.lowStockProductsCount,
                onClickViewAll = {
                    if (dashboardData.pendingOrdersCount > 0) onNavigateToTab(2) else onNavigateToTab(1)
                }
            )
        }

        // --- Weekly Performance Bar Chart ---
        item {
            WeeklyPerformanceGraph()
        }

        // --- Order Allocation Ratio (Custom Canvas Chart) ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF111111))
                    .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "ORDERS ALLOCATION RATIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OrderRatioChart(
                        pending = dashboardData.pendingOrdersCount,
                        delivered = dashboardData.deliveredOrdersCount,
                        cancelled = dashboardData.cancelledOrdersCount,
                        processing = dashboardData.processingOrdersCount,
                        shipped = dashboardData.shippedOrdersCount
                    )
                }
            }
        }

        // --- Low Stock Alerts Drawer Section ---
        if (dashboardData.lowStockProductsList.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = "Alert",
                        tint = WarningYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Critical Low Stock Alert",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningYellow
                    )
                }
            }

            items(dashboardData.lowStockProductsList.take(3)) { prod ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x11FFB300))
                        .border(1.dp, WarningYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = prod.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "SKU: ${prod.sku} | Category: ${prod.category}",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "Stock: ${prod.stockQuantity} Pcs",
                        fontWeight = FontWeight.ExtraBold,
                        color = ErrorRed,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun QuickAlertSection(
    pendingCount: Int,
    lowStockCount: Int,
    onClickViewAll: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val alertText = if (pendingCount > 0) {
        "$pendingCount Orders need immediate shipping"
    } else {
        "$lowStockCount Products are low in stock"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PrimaryRed.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                )
            )
            .border(1.dp, PrimaryRed.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed.copy(alpha = dotAlpha))
                )
                Text(
                    text = alertText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Button(
                onClick = onClickViewAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "VIEW ALL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeeklyPerformanceGraph() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEEKLY PERFORMANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
            Text(
                text = "+24% vs Last Week",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF111111))
                .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.95f, 0.65f)
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                heights.forEachIndexed { index, h ->
                    val isToday = index == 5 // Saturday highlighted
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isToday) PrimaryRed else PrimaryRed.copy(alpha = 0.25f)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = days[index],
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderRatioChart(
    pending: Int,
    processing: Int,
    shipped: Int,
    delivered: Int,
    cancelled: Int
) {
    val total = (pending + processing + shipped + delivered + cancelled).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        if (total == 0f) {
            drawRoundRect(
                color = Color.DarkGray,
                size = Size(size.width, 14.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            return@Canvas
        }

        val pendingW = (pending / total) * size.width
        val processingW = (processing / total) * size.width
        val shippedW = (shipped / total) * size.width
        val deliveredW = (delivered / total) * size.width
        val cancelledW = (cancelled / total) * size.width

        var currentX = 0f
        val barHeight = 16.dp.toPx()

        if (pendingW > 0) {
            drawRect(
                color = WarningYellow,
                topLeft = Offset(currentX, 0f),
                size = Size(pendingW, barHeight)
            )
            currentX += pendingW
        }
        if (processingW > 0) {
            drawRect(
                color = Color.Cyan,
                topLeft = Offset(currentX, 0f),
                size = Size(processingW, barHeight)
            )
            currentX += processingW
        }
        if (shippedW > 0) {
            drawRect(
                color = Color.Blue,
                topLeft = Offset(currentX, 0f),
                size = Size(shippedW, barHeight)
            )
            currentX += shippedW
        }
        if (deliveredW > 0) {
            drawRect(
                color = SuccessGreen,
                topLeft = Offset(currentX, 0f),
                size = Size(deliveredW, barHeight)
            )
            currentX += deliveredW
        }
        if (cancelledW > 0) {
            drawRect(
                color = ErrorRed,
                topLeft = Offset(currentX, 0f),
                size = Size(cancelledW, barHeight)
            )
        }

        // Draw Legends
        val legendY = 40.dp.toPx()
        val spacing = size.width / 5

        drawCircle(color = WarningYellow, radius = 4.dp.toPx(), center = Offset(10.dp.toPx(), legendY + 12.dp.toPx()))
        drawCircle(color = SuccessGreen, radius = 4.dp.toPx(), center = Offset(spacing + 10.dp.toPx(), legendY + 12.dp.toPx()))
        drawCircle(color = ErrorRed, radius = 4.dp.toPx(), center = Offset(spacing * 2 + 10.dp.toPx(), legendY + 12.dp.toPx()))
        drawCircle(color = Color.Cyan, radius = 4.dp.toPx(), center = Offset(spacing * 3 + 10.dp.toPx(), legendY + 12.dp.toPx()))
        drawCircle(color = Color.Blue, radius = 4.dp.toPx(), center = Offset(spacing * 4 + 10.dp.toPx(), legendY + 12.dp.toPx()))
    }
}
