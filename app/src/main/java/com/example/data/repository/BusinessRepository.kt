package com.example.data.repository

import com.example.data.database.AppDao
import com.example.data.model.ProductEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.StockHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BusinessRepository(private val appDao: AppDao) {

    private val mutex = Mutex()

    val products: Flow<List<ProductEntity>> = appDao.getAllProducts()
    val orders: Flow<List<OrderEntity>> = appDao.getAllOrders()
    val expenses: Flow<List<ExpenseEntity>> = appDao.getAllExpenses()
    val stockHistory: Flow<List<StockHistoryEntity>> = appDao.getAllStockHistory()

    fun getProductById(id: Long): Flow<ProductEntity?> = appDao.getProductById(id)
    fun getOrderById(id: Long): Flow<OrderEntity?> = appDao.getOrderById(id)
    fun getStockHistoryForProduct(productId: Long): Flow<List<StockHistoryEntity>> = appDao.getStockHistoryForProduct(productId)

    // --- PRODUCTS ---
    suspend fun addProduct(product: ProductEntity): Long = mutex.withLock {
        val id = appDao.insertProduct(product)
        appDao.insertStockHistory(
            StockHistoryEntity(
                productId = id,
                productName = product.name,
                type = "ADD",
                quantityChange = product.stockQuantity,
                currentStock = product.stockQuantity,
                notes = "Initial stock added for ${product.name}"
            )
        )
        return id
    }

    suspend fun updateProduct(product: ProductEntity) = mutex.withLock {
        val oldProduct = appDao.getProductByIdOneShot(product.id)
        appDao.updateProduct(product)
        
        if (oldProduct != null && oldProduct.stockQuantity != product.stockQuantity) {
            val change = product.stockQuantity - oldProduct.stockQuantity
            appDao.insertStockHistory(
                StockHistoryEntity(
                    productId = product.id,
                    productName = product.name,
                    type = "EDIT_STOCK",
                    quantityChange = change,
                    currentStock = product.stockQuantity,
                    notes = "Stock adjusted from ${oldProduct.stockQuantity} to ${product.stockQuantity}"
                )
            )
        }
    }

    suspend fun deleteProduct(id: Long) = mutex.withLock {
        appDao.deleteProductById(id)
    }

    // --- ORDERS ---
    suspend fun createOrder(order: OrderEntity): Result<Long> = mutex.withLock {
        // Double-check product stock
        val product = appDao.getProductByIdOneShot(order.productId)
            ?: return Result.failure(Exception("Product not found"))

        // Create the order
        val id = appDao.insertOrder(order)
        
        // If order is created directly with "Delivered" status
        if (order.status == "Delivered") {
            if (product.stockQuantity < order.quantity) {
                // If insufficient stock, rollback order or throw error
                return Result.failure(Exception("Insufficient stock in inventory for ${product.name}. Required: ${order.quantity}, Available: ${product.stockQuantity}"))
            }
            
            val newStock = product.stockQuantity - order.quantity
            appDao.updateProduct(product.copy(stockQuantity = newStock))
            
            appDao.insertStockHistory(
                StockHistoryEntity(
                    productId = product.id,
                    productName = product.name,
                    type = "DELIVERY_DEDUCT",
                    quantityChange = -order.quantity,
                    currentStock = newStock,
                    notes = "Order #${order.invoiceNumber} delivered. Stock deducted."
                )
            )
        }
        
        return Result.success(id)
    }

    suspend fun updateOrder(order: OrderEntity): Result<Unit> = mutex.withLock {
        val oldOrder = appDao.getOrderByIdOneShot(order.id)
            ?: return Result.failure(Exception("Order not found"))

        if (oldOrder.status == order.status) {
            // No status transition, just update order details
            appDao.updateOrder(order)
            return Result.success(Unit)
        }

        val product = appDao.getProductByIdOneShot(order.productId)
            ?: return Result.failure(Exception("Associated product not found"))

        var updatedProduct = product
        
        // 1. Rollback effects of OLD status if it was "Delivered"
        if (oldOrder.status == "Delivered") {
            val restoredStock = updatedProduct.stockQuantity + oldOrder.quantity
            updatedProduct = updatedProduct.copy(stockQuantity = restoredStock)
            appDao.updateProduct(updatedProduct)
            appDao.insertStockHistory(
                StockHistoryEntity(
                    productId = updatedProduct.id,
                    productName = updatedProduct.name,
                    type = "CANCEL_RESTORE",
                    quantityChange = oldOrder.quantity,
                    currentStock = restoredStock,
                    notes = "Status changed from Delivered. Stock restored."
                )
            )
        }

        // 2. Apply effects of NEW status
        if (order.status == "Delivered") {
            if (updatedProduct.stockQuantity < order.quantity) {
                return Result.failure(Exception("Insufficient stock in inventory for ${updatedProduct.name}. Required: ${order.quantity}, Available: ${updatedProduct.stockQuantity}"))
            }
            val decreasedStock = updatedProduct.stockQuantity - order.quantity
            updatedProduct = updatedProduct.copy(stockQuantity = decreasedStock)
            appDao.updateProduct(updatedProduct)
            appDao.insertStockHistory(
                StockHistoryEntity(
                    productId = updatedProduct.id,
                    productName = updatedProduct.name,
                    type = "DELIVERY_DEDUCT",
                    quantityChange = -order.quantity,
                    currentStock = decreasedStock,
                    notes = "Order #${order.invoiceNumber} delivered. Stock deducted."
                )
            )
        }

        // Update order date parameters for history
        val orderToSave = order.copy(
            deliveryDate = if (order.status == "Delivered") System.currentTimeMillis() else null,
            cancelledDate = if (order.status == "Cancelled") System.currentTimeMillis() else null
        )

        appDao.updateOrder(orderToSave)
        return Result.success(Unit)
    }

    suspend fun deleteOrder(order: OrderEntity) = mutex.withLock {
        // If it was delivered, restore stock upon deletion as a safety measure
        if (order.status == "Delivered") {
            val product = appDao.getProductByIdOneShot(order.productId)
            if (product != null) {
                val restoredStock = product.stockQuantity + order.quantity
                appDao.updateProduct(product.copy(stockQuantity = restoredStock))
                appDao.insertStockHistory(
                    StockHistoryEntity(
                        productId = product.id,
                        productName = product.name,
                        type = "CANCEL_RESTORE",
                        quantityChange = order.quantity,
                        currentStock = restoredStock,
                        notes = "Order #${order.invoiceNumber} deleted. Stock restored."
                    )
                )
            }
        }
        appDao.deleteOrder(order)
    }

    // --- EXPENSES ---
    suspend fun addExpense(expense: ExpenseEntity): Long = mutex.withLock {
        appDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = mutex.withLock {
        appDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = mutex.withLock {
        appDao.deleteExpense(expense)
    }
}
