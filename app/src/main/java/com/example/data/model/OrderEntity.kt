package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String, // e.g. DS-2026-0001
    val customerName: String,
    val phoneNumber: String,
    val address: String,
    val courier: String, // Pathao, Steadfast, RedX, etc.
    val productId: Long,
    val productName: String,
    val size: String,
    val color: String,
    val quantity: Int,
    val sellingPrice: Double,
    val buyingPrice: Double,
    val discount: Double,
    val deliveryCharge: Double,
    val advancePayment: Double,
    val paymentStatus: String, // Unpaid, Paid, Partially Paid
    val notes: String,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String, // Pending, Processing, Shipped, Delivered, Cancelled
    val deliveryDate: Long? = null,
    val cancelledDate: Long? = null
)
