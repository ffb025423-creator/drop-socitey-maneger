package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Shop Rent, Packaging, Advertising, Courier Cost, Transport, Internet, Electricity, Employee Salary, Miscellaneous
    val amount: Double,
    val notes: String,
    val date: Long = System.currentTimeMillis()
)
