package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DashboardData
import com.example.data.model.ExpenseEntity
import com.example.data.model.InAppNotification
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ReportsData
import com.example.data.model.CustomerProfile
import com.example.data.model.StockHistoryEntity
import com.example.data.preference.PreferencesManager
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BusinessRepository(database.appDao())
    val prefs = PreferencesManager(application)

    init {
        viewModelScope.launch {
            // Check if database is empty to seed mock data
            val currentProds = database.appDao().getAllProducts().first().ifEmpty { emptyList() }
            if (currentProds.isEmpty()) {
                seedDefaultData()
            }
        }
    }

    private suspend fun seedDefaultData() {
        val p1 = ProductEntity(
            name = "Argentina Player Edition Jersey",
            category = "Jersey",
            sku = "JER-ARG-01",
            buyingPrice = 650.0,
            sellingPrice = 1150.0,
            stockQuantity = 45,
            sizes = listOf("S", "M", "L", "XL"),
            colors = listOf("Blue & White"),
            images = emptyList(),
            barcode = "JER-ARG-01",
            qrCode = "JER-ARG-01",
            description = "Player edition premium mesh jersey with heat pressed logo.",
            status = "Active",
            lowStockThreshold = 5
        )
        val id1 = repository.addProduct(p1)

        val p2 = ProductEntity(
            name = "Drop Shoulder Plain Black Tee",
            category = "Drop Shoulder",
            sku = "DS-TEE-02",
            buyingPrice = 380.0,
            sellingPrice = 750.0,
            stockQuantity = 52,
            sizes = listOf("M", "L", "XL"),
            colors = listOf("Black"),
            images = emptyList(),
            barcode = "DS-TEE-02",
            qrCode = "DS-TEE-02",
            description = "Heavyweight 240 GSM pre-shrunk cotton oversized t-shirt.",
            status = "Active",
            lowStockThreshold = 5
        )
        val id2 = repository.addProduct(p2)

        val p3 = ProductEntity(
            name = "Classic Pique Polo Shirt",
            category = "Polo Shirt",
            sku = "POL-CLS-03",
            buyingPrice = 520.0,
            sellingPrice = 980.0,
            stockQuantity = 3, // Low Stock Alert Trigger!
            sizes = listOf("M", "L", "XL"),
            colors = listOf("Navy Blue", "Olive Green"),
            images = emptyList(),
            barcode = "POL-CLS-03",
            qrCode = "POL-CLS-03",
            description = "Premium polo shirt with ribbed cuffs and structured collars.",
            status = "Active",
            lowStockThreshold = 5
        )
        val id3 = repository.addProduct(p3)

        val p4 = ProductEntity(
            name = "Winter Heavyweight Hoodie",
            category = "Hoodie",
            sku = "HOD-WNT-04",
            buyingPrice = 1100.0,
            sellingPrice = 1850.0,
            stockQuantity = 18,
            sizes = listOf("S", "M", "L", "XL"),
            colors = listOf("Heather Charcoal"),
            images = emptyList(),
            barcode = "HOD-WNT-04",
            qrCode = "HOD-WNT-04",
            description = "Fleece-lined winter hoodie with spacious kangaroo pockets.",
            status = "Active",
            lowStockThreshold = 5
        )
        val id4 = repository.addProduct(p4)

        // Seed Orders
        val o1 = OrderEntity(
            invoiceNumber = "DS-2026-0001",
            customerName = "Ariful Islam",
            phoneNumber = "+8801711223344",
            address = "Dhanmondi 27, Dhaka",
            courier = "Steadfast",
            productId = id1,
            productName = "Argentina Player Edition Jersey",
            size = "L",
            color = "Blue & White",
            quantity = 2,
            sellingPrice = 1150.0,
            buyingPrice = 650.0,
            discount = 50.0,
            deliveryCharge = 120.0,
            advancePayment = 0.0,
            paymentStatus = "Unpaid",
            notes = "Deliver urgently",
            status = "Delivered",
            orderDate = System.currentTimeMillis() - 86400000, // Yesterday
            deliveryDate = System.currentTimeMillis() - 43200000
        )
        // Insert and update stock manually/directly
        database.appDao().insertOrder(o1)
        // Deduct stock for delivered o1
        database.appDao().updateProduct(p1.copy(id = id1, stockQuantity = p1.stockQuantity - 2))

        val o2 = OrderEntity(
            invoiceNumber = "DS-2026-0002",
            customerName = "Sultana Razia",
            phoneNumber = "+8801822334455",
            address = "Chashara, Narayanganj",
            courier = "Pathao",
            productId = id2,
            productName = "Drop Shoulder Plain Black Tee",
            size = "M",
            color = "Black",
            quantity = 1,
            sellingPrice = 750.0,
            buyingPrice = 380.0,
            discount = 0.0,
            deliveryCharge = 120.0,
            advancePayment = 150.0,
            paymentStatus = "Partially Paid",
            notes = "Call before delivery",
            status = "Pending",
            orderDate = System.currentTimeMillis()
        )
        database.appDao().insertOrder(o2)

        val o3 = OrderEntity(
            invoiceNumber = "DS-2026-0003",
            customerName = "Tanvir Ahmed",
            phoneNumber = "+8801933445566",
            address = "GEC Circle, Chittagong",
            courier = "RedX",
            productId = id4,
            productName = "Winter Heavyweight Hoodie",
            size = "XL",
            color = "Heather Charcoal",
            quantity = 1,
            sellingPrice = 1850.0,
            buyingPrice = 1100.0,
            discount = 0.0,
            deliveryCharge = 150.0,
            advancePayment = 0.0,
            paymentStatus = "Unpaid",
            notes = "Customer changed mind",
            status = "Cancelled",
            orderDate = System.currentTimeMillis() - 172800000,
            cancelledDate = System.currentTimeMillis() - 86400000
        )
        database.appDao().insertOrder(o3)

        // Seed Expenses
        database.appDao().insertExpense(
            ExpenseEntity(
                category = "Shop Rent",
                amount = 12000.0,
                notes = "Monthly shop rental share",
                date = System.currentTimeMillis() - 172800000
            )
        )
        database.appDao().insertExpense(
            ExpenseEntity(
                category = "Packaging",
                amount = 1500.0,
                notes = "Poly packing material and drop brand stickers",
                date = System.currentTimeMillis() - 86400000
            )
        )
        database.appDao().insertExpense(
            ExpenseEntity(
                category = "Advertising",
                amount = 4500.0,
                notes = "Facebook promotional campaign",
                date = System.currentTimeMillis() - 43200000
            )
        )
    }

    // Live state for settings inside UI
    val isDarkTheme = mutableStateOf(prefs.isDarkTheme())
    val currencySymbol = mutableStateOf(prefs.getCurrencySymbol())
    val dateFormat = mutableStateOf(prefs.getDateFormat())
    val securityPin = mutableStateOf(prefs.getSecurityPin())
    val isFingerprintEnabled = mutableStateOf(prefs.isFingerprintEnabled())
    val isAutoBackupEnabled = mutableStateOf(prefs.isAutoBackupEnabled())

    // Base flows
    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockHistory: StateFlow<List<StockHistoryEntity>> = repository.stockHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toggle theme
    fun toggleDarkTheme(value: Boolean) {
        prefs.setDarkTheme(value)
        isDarkTheme.value = value
    }

    // Set currency symbol
    fun setCurrency(value: String) {
        prefs.setCurrencySymbol(value)
        currencySymbol.value = value
    }

    // Set Date format
    fun setDateFormatString(value: String) {
        prefs.setDateFormat(value)
        dateFormat.value = value
    }

    // Set PIN
    fun setPin(pin: String) {
        prefs.setSecurityPin(pin)
        securityPin.value = pin
    }

    fun setFingerprint(enabled: Boolean) {
        prefs.setFingerprintEnabled(enabled)
        isFingerprintEnabled.value = enabled
    }

    fun setAutoBackup(enabled: Boolean) {
        prefs.setAutoBackupEnabled(enabled)
        isAutoBackupEnabled.value = enabled
    }

    // 1. Dynamic Dashboard State Combination
    val dashboardState: StateFlow<DashboardData> = combine(products, orders, expenses) { prodList, ordList, expList ->
        val totalProd = prodList.size
        val totalCat = prodList.map { it.category }.distinct().size
        
        val pending = ordList.count { it.status == "Pending" }
        val processing = ordList.count { it.status == "Processing" }
        val shipped = ordList.count { it.status == "Shipped" }
        val delivered = ordList.count { it.status == "Delivered" }
        val cancelled = ordList.count { it.status == "Cancelled" }

        val today = Calendar.getInstance()
        
        val todaySales = ordList.filter { it.status == "Delivered" && isSameDay(it.orderDate, today) }
            .sumOf { (it.sellingPrice - it.discount) * it.quantity }

        val todayProfit = ordList.filter { it.status == "Delivered" && isSameDay(it.orderDate, today) }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val monthlyProfit = ordList.filter { it.status == "Delivered" && isSameMonth(it.orderDate, today) }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val totalRev = ordList.filter { it.status == "Delivered" }
            .sumOf { (it.sellingPrice - it.discount) * it.quantity }

        val totalProf = ordList.filter { it.status == "Delivered" }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val totalExp = expList.sumOf { it.amount }
        val netProf = totalProf - totalExp

        val lowStock = prodList.filter { it.stockQuantity <= it.lowStockThreshold }

        DashboardData(
            totalProducts = totalProd,
            totalCategories = totalCat,
            pendingOrdersCount = pending,
            processingOrdersCount = processing,
            shippedOrdersCount = shipped,
            deliveredOrdersCount = delivered,
            cancelledOrdersCount = cancelled,
            todaySales = todaySales,
            todayProfit = todayProfit,
            monthlyProfit = monthlyProfit,
            totalRevenue = totalRev,
            totalProfit = totalProf,
            totalExpenses = totalExp,
            netProfit = netProf,
            lowStockProductsCount = lowStock.size,
            lowStockProductsList = lowStock
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardData())

    // 2. Dynamic Reports State Combination
    val reportsState: StateFlow<ReportsData> = combine(products, orders, expenses) { prodList, ordList, expList ->
        val today = Calendar.getInstance()
        
        val deliveredOrders = ordList.filter { it.status == "Delivered" }
        
        val dailyProf = deliveredOrders.filter { isSameDay(it.orderDate, today) }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val weeklyProf = deliveredOrders.filter { isSameWeek(it.orderDate, today) }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val monthlyProf = deliveredOrders.filter { isSameMonth(it.orderDate, today) }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val yearlyProf = deliveredOrders.filter { isSameYear(it.orderDate, today) }
            .sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

        val totalRev = deliveredOrders.sumOf { (it.sellingPrice - it.discount) * it.quantity }
        val totalExp = expList.sumOf { it.amount }
        
        val grossProf = deliveredOrders.sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }
        val netProf = grossProf - totalExp

        val totalDisc = deliveredOrders.sumOf { it.discount * it.quantity }
        val totalDelivery = deliveredOrders.sumOf { it.deliveryCharge }

        val cancelledCount = ordList.count { it.status == "Cancelled" }
        val deliveredCount = deliveredOrders.size

        // Top Selling Products
        val topSelling = deliveredOrders.groupBy { it.productName }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        // Top Categories
        val prodCategoryMap = prodList.associate { it.id to it.category }
        val topCats = deliveredOrders.groupBy { ord -> prodCategoryMap[ord.productId] ?: "Unknown" }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        // Top Sizes
        val topSizes = deliveredOrders.groupBy { it.size }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        ReportsData(
            dailyProfit = dailyProf,
            weeklyProfit = weeklyProf,
            monthlyProfit = monthlyProf,
            yearlyProfit = yearlyProf,
            totalRevenue = totalRev,
            totalExpenses = totalExp,
            grossProfit = grossProf,
            netProfit = netProf,
            totalDiscount = totalDisc,
            totalDeliveryCharges = totalDelivery,
            cancelledOrdersCount = cancelledCount,
            deliveredOrdersCount = deliveredCount,
            topSellingProducts = topSelling,
            topCategories = topCats,
            topSizes = topSizes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsData())

    // 3. Dynamic Customer Profiles Combination
    val customersList: StateFlow<List<CustomerProfile>> = orders.map { ordList ->
        ordList.groupBy { it.phoneNumber.trim() }
            .map { (phone, ords) ->
                val sortedOrds = ords.sortedByDescending { it.orderDate }
                val latest = sortedOrds.first()
                val delivered = ords.filter { it.status == "Delivered" }
                val totalSpend = delivered.sumOf { (it.sellingPrice - it.discount) * it.quantity + it.deliveryCharge }
                val totalProfit = delivered.sumOf { ((it.sellingPrice - it.discount) - it.buyingPrice) * it.quantity }

                CustomerProfile(
                    name = latest.customerName,
                    phoneNumber = phone,
                    address = latest.address,
                    totalOrders = ords.size,
                    pendingCount = ords.count { it.status == "Pending" },
                    deliveredCount = ords.count { it.status == "Delivered" },
                    cancelledCount = ords.count { it.status == "Cancelled" },
                    totalSpending = totalSpend,
                    totalProfit = totalProfit,
                    lastOrderDate = latest.orderDate,
                    orderHistory = sortedOrds
                )
            }.sortedByDescending { it.totalSpending }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. In-App Notification Flow
    val notificationsList: StateFlow<List<InAppNotification>> = combine(products, orders) { prodList, ordList ->
        val list = mutableListOf<InAppNotification>()
        var idCounter = 1

        // Low stock notifications
        prodList.filter { it.stockQuantity <= it.lowStockThreshold }.forEach { prod ->
            list.add(
                InAppNotification(
                    id = "low_stock_${idCounter++}",
                    title = "Low Stock Alert",
                    message = "${prod.name} has low stock: ${prod.stockQuantity} remaining.",
                    type = "WARNING"
                )
            )
        }

        // Pending orders reminder
        val pendingCount = ordList.count { it.status == "Pending" }
        if (pendingCount > 0) {
            list.add(
                InAppNotification(
                    id = "pending_orders_${idCounter++}",
                    title = "Pending Orders Queue",
                    message = "You have $pendingCount pending orders waiting to be processed.",
                    type = "INFO"
                )
            )
        }

        // Delivered order notifications
        val recentDelivered = ordList.filter { it.status == "Delivered" && isSameDay(it.deliveryDate ?: 0L, Calendar.getInstance()) }
        if (recentDelivered.isNotEmpty()) {
            list.add(
                InAppNotification(
                    id = "delivered_today_${idCounter++}",
                    title = "Orders Delivered Today",
                    message = "Successfully delivered ${recentDelivered.size} orders today.",
                    type = "SUCCESS"
                )
            )
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CRUD WRAPPERS ---
    fun saveProduct(product: ProductEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                if (product.id == 0L) {
                    repository.addProduct(product)
                    onResult(true, "Product added successfully")
                } else {
                    repository.updateProduct(product)
                    onResult(true, "Product updated successfully")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to save product")
            }
        }
    }

    fun deleteProduct(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(id)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun saveOrder(order: OrderEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                if (order.id == 0L) {
                    val result = repository.createOrder(order)
                    if (result.isSuccess) {
                        onResult(true, "Order created successfully with Invoice ${order.invoiceNumber}")
                    } else {
                        onResult(false, result.exceptionOrNull()?.message ?: "Failed to create order")
                    }
                } else {
                    val result = repository.updateOrder(order)
                    if (result.isSuccess) {
                        onResult(true, "Order updated successfully")
                    } else {
                        onResult(false, result.exceptionOrNull()?.message ?: "Failed to update order")
                    }
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to save order")
            }
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val ordFlow = repository.getOrderById(orderId).firstOrNull()
                if (ordFlow != null) {
                    val updated = ordFlow.copy(status = newStatus)
                    val result = repository.updateOrder(updated)
                    if (result.isSuccess) {
                        onResult(true, "Order status updated to $newStatus")
                    } else {
                        onResult(false, result.exceptionOrNull()?.message ?: "Failed to update status")
                    }
                } else {
                    onResult(false, "Order not found")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to update status")
            }
        }
    }

    fun deleteOrder(order: OrderEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteOrder(order)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun saveExpense(expense: ExpenseEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (expense.id == 0L) {
                    repository.addExpense(expense)
                } else {
                    repository.updateExpense(expense)
                }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // --- BACKUP & RESTORE LOGIC ---
    fun exportBackup(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val prodList = products.value
                val ordList = orders.value
                val expList = expenses.value
                val histList = stockHistory.value

                val backupJson = JSONObject()

                // Products array
                val prodArray = JSONArray()
                prodList.forEach { p ->
                    prodArray.put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("category", p.category)
                        put("sku", p.sku)
                        put("buyingPrice", p.buyingPrice)
                        put("sellingPrice", p.sellingPrice)
                        put("stockQuantity", p.stockQuantity)
                        put("sizes", JSONArray(p.sizes))
                        put("colors", JSONArray(p.colors))
                        put("images", JSONArray(p.images))
                        put("barcode", p.barcode)
                        put("qrCode", p.qrCode)
                        put("description", p.description)
                        put("status", p.status)
                        put("lowStockThreshold", p.lowStockThreshold)
                        put("timestamp", p.timestamp)
                    })
                }
                backupJson.put("products", prodArray)

                // Orders array
                val ordArray = JSONArray()
                ordList.forEach { o ->
                    ordArray.put(JSONObject().apply {
                        put("id", o.id)
                        put("invoiceNumber", o.invoiceNumber)
                        put("customerName", o.customerName)
                        put("phoneNumber", o.phoneNumber)
                        put("address", o.address)
                        put("courier", o.courier)
                        put("productId", o.productId)
                        put("productName", o.productName)
                        put("size", o.size)
                        put("color", o.color)
                        put("quantity", o.quantity)
                        put("sellingPrice", o.sellingPrice)
                        put("buyingPrice", o.buyingPrice)
                        put("discount", o.discount)
                        put("deliveryCharge", o.deliveryCharge)
                        put("advancePayment", o.advancePayment)
                        put("paymentStatus", o.paymentStatus)
                        put("notes", o.notes)
                        put("orderDate", o.orderDate)
                        put("status", o.status)
                        if (o.deliveryDate != null) put("deliveryDate", o.deliveryDate)
                        if (o.cancelledDate != null) put("cancelledDate", o.cancelledDate)
                    })
                }
                backupJson.put("orders", ordArray)

                // Expenses array
                val expArray = JSONArray()
                expList.forEach { e ->
                    expArray.put(JSONObject().apply {
                        put("id", e.id)
                        put("category", e.category)
                        put("amount", e.amount)
                        put("notes", e.notes)
                        put("date", e.date)
                    })
                }
                backupJson.put("expenses", expArray)

                // History array
                val histArray = JSONArray()
                histList.forEach { h ->
                    histArray.put(JSONObject().apply {
                        put("id", h.id)
                        put("productId", h.productId)
                        put("productName", h.productName)
                        put("type", h.type)
                        put("quantityChange", h.quantityChange)
                        put("currentStock", h.currentStock)
                        put("timestamp", h.timestamp)
                        put("notes", h.notes)
                    })
                }
                backupJson.put("stockHistory", histArray)

                // Write to cache dir file
                val cacheDir = getApplication<Application>().cacheDir
                val backupFile = File(cacheDir, "drop_society_backup.json")
                backupFile.writeText(backupJson.toString(2))

                onResult(true, backupFile.absolutePath)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to generate backup JSON")
            }
        }
    }

    fun restoreBackup(backupJsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = JSONObject(backupJsonString)

                // Delete entire database tables safely
                database.clearAllTables()

                // Restore Products
                if (json.has("products")) {
                    val arr = json.getJSONArray("products")
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        
                        val sizesList = mutableListOf<String>()
                        val sArr = o.getJSONArray("sizes")
                        for (s in 0 until sArr.length()) sizesList.add(sArr.getString(s))

                        val colorsList = mutableListOf<String>()
                        val cArr = o.getJSONArray("colors")
                        for (c in 0 until cArr.length()) colorsList.add(cArr.getString(c))

                        val imagesList = mutableListOf<String>()
                        val iArr = o.getJSONArray("images")
                        for (im in 0 until iArr.length()) imagesList.add(iArr.getString(im))

                        val p = ProductEntity(
                            id = o.optLong("id", 0L),
                            name = o.getString("name"),
                            category = o.getString("category"),
                            sku = o.getString("sku"),
                            buyingPrice = o.getDouble("buyingPrice"),
                            sellingPrice = o.getDouble("sellingPrice"),
                            stockQuantity = o.getInt("stockQuantity"),
                            sizes = sizesList,
                            colors = colorsList,
                            images = imagesList,
                            barcode = o.getString("barcode"),
                            qrCode = o.getString("qrCode"),
                            description = o.getString("description"),
                            status = o.getString("status"),
                            lowStockThreshold = o.optInt("lowStockThreshold", 5),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                        database.appDao().insertProduct(p)
                    }
                }

                // Restore Orders
                if (json.has("orders")) {
                    val arr = json.getJSONArray("orders")
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val order = OrderEntity(
                            id = o.optLong("id", 0L),
                            invoiceNumber = o.getString("invoiceNumber"),
                            customerName = o.getString("customerName"),
                            phoneNumber = o.getString("phoneNumber"),
                            address = o.getString("address"),
                            courier = o.getString("courier"),
                            productId = o.getLong("productId"),
                            productName = o.getString("productName"),
                            size = o.getString("size"),
                            color = o.getString("color"),
                            quantity = o.getInt("quantity"),
                            sellingPrice = o.getDouble("sellingPrice"),
                            buyingPrice = o.getDouble("buyingPrice"),
                            discount = o.getDouble("discount"),
                            deliveryCharge = o.getDouble("deliveryCharge"),
                            advancePayment = o.getDouble("advancePayment"),
                            paymentStatus = o.getString("paymentStatus"),
                            notes = o.getString("notes"),
                            orderDate = o.optLong("orderDate", System.currentTimeMillis()),
                            status = o.getString("status"),
                            deliveryDate = if (o.has("deliveryDate")) o.getLong("deliveryDate") else null,
                            cancelledDate = if (o.has("cancelledDate")) o.getLong("cancelledDate") else null
                        )
                        database.appDao().insertOrder(order)
                    }
                }

                // Restore Expenses
                if (json.has("expenses")) {
                    val arr = json.getJSONArray("expenses")
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val exp = ExpenseEntity(
                            id = o.optLong("id", 0L),
                            category = o.getString("category"),
                            amount = o.getDouble("amount"),
                            notes = o.getString("notes"),
                            date = o.optLong("date", System.currentTimeMillis())
                        )
                        database.appDao().insertExpense(exp)
                    }
                }

                // Restore StockHistory
                if (json.has("stockHistory")) {
                    val arr = json.getJSONArray("stockHistory")
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val hist = StockHistoryEntity(
                            id = o.optLong("id", 0L),
                            productId = o.getLong("productId"),
                            productName = o.getString("productName"),
                            type = o.getString("type"),
                            quantityChange = o.getInt("quantityChange"),
                            currentStock = o.getInt("currentStock"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            notes = o.getString("notes")
                        )
                        database.appDao().insertStockHistory(hist)
                    }
                }

                onResult(true, "Database restored successfully!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to parse and restore JSON")
            }
        }
    }

    fun purgeAllData(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                database.clearAllTables()
                prefs.clearAllData()
                
                // Reset live states
                isDarkTheme.value = true
                currencySymbol.value = "৳"
                dateFormat.value = "dd MMM yyyy"
                securityPin.value = ""
                isFingerprintEnabled.value = false
                isAutoBackupEnabled.value = false
                
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // --- DATE HELPERS ---
    private fun isSameDay(t1: Long, cal: Calendar): Boolean {
        val c2 = Calendar.getInstance().apply { timeInMillis = t1 }
        return cal.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(t1: Long, cal: Calendar): Boolean {
        val c2 = Calendar.getInstance().apply { timeInMillis = t1 }
        return cal.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                cal.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(t1: Long, cal: Calendar): Boolean {
        val c2 = Calendar.getInstance().apply { timeInMillis = t1 }
        return cal.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
    }

    private fun isSameYear(t1: Long, cal: Calendar): Boolean {
        val c2 = Calendar.getInstance().apply { timeInMillis = t1 }
        return cal.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }
}
