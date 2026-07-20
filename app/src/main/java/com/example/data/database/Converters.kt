package com.example.data.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        return value.joinToString("|||") { it.replace("|||", "") } // Use uncommon delimiter to avoid collisions
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        return value.split("|||").filter { it.isNotEmpty() }
    }
}
