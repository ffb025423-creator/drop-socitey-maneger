package com.example.data.database

import androidx.room.*
import com.example.data.model.ProductEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.StockHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdOneShot(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)


    // --- ORDERS ---
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderByIdOneShot(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)


    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)


    // --- STOCK HISTORY ---
    @Query("SELECT * FROM stock_history ORDER BY timestamp DESC")
    fun getAllStockHistory(): Flow<List<StockHistoryEntity>>

    @Query("SELECT * FROM stock_history WHERE productId = :productId ORDER BY timestamp DESC")
    fun getStockHistoryForProduct(productId: Long): Flow<List<StockHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(history: StockHistoryEntity): Long
}
