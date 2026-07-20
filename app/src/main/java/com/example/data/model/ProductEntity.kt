package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Jersey, Drop Shoulder, Polo Shirt, Hoodie, Pant, Custom Category
    val sku: String,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val sizes: List<String>, // S, M, L, XL, etc.
    val colors: List<String>, // Red, Black, etc.
    val images: List<String>, // Multiple image URI/path strings
    val barcode: String,
    val qrCode: String,
    val description: String,
    val status: String, // "Active", "Inactive"
    val lowStockThreshold: Int = 5,
    val timestamp: Long = System.currentTimeMillis()
)
