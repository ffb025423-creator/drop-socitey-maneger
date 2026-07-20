package com.example.data.model

data class DashboardData(
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val pendingOrdersCount: Int = 0,
    val processingOrdersCount: Int = 0,
    val shippedOrdersCount: Int = 0,
    val deliveredOrdersCount: Int = 0,
    val cancelledOrdersCount: Int = 0,
    val todaySales: Double = 0.0,
    val todayProfit: Double = 0.0,
    val monthlyProfit: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val lowStockProductsCount: Int = 0,
    val lowStockProductsList: List<ProductEntity> = emptyList()
)

data class ReportsData(
    val dailyProfit: Double = 0.0,
    val weeklyProfit: Double = 0.0,
    val monthlyProfit: Double = 0.0,
    val yearlyProfit: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grossProfit: Double = 0.0, // SellingPrice - BuyingPrice before expenses
    val netProfit: Double = 0.0, // grossProfit - expenses
    val totalDiscount: Double = 0.0,
    val totalDeliveryCharges: Double = 0.0,
    val cancelledOrdersCount: Int = 0,
    val deliveredOrdersCount: Int = 0,
    val topSellingProducts: List<Pair<String, Int>> = emptyList(), // Product Name to Quantity
    val topCategories: List<Pair<String, Int>> = emptyList(), // Category to Quantity
    val topSizes: List<Pair<String, Int>> = emptyList() // Size to Quantity
)

data class CustomerProfile(
    val name: String,
    val phoneNumber: String,
    val address: String,
    val totalOrders: Int = 0,
    val pendingCount: Int = 0,
    val deliveredCount: Int = 0,
    val cancelledCount: Int = 0,
    val totalSpending: Double = 0.0,
    val totalProfit: Double = 0.0,
    val lastOrderDate: Long = 0L,
    val orderHistory: List<OrderEntity> = emptyList()
)

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "INFO", "WARNING", "SUCCESS"
    val timestamp: Long = System.currentTimeMillis()
)
