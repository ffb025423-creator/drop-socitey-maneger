package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_history")
data class StockHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // ADD, EDIT_STOCK, DELIVERY_DEDUCT, CANCEL_RESTORE
    val quantityChange: Int,
    val currentStock: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String
)
