package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "picks")
data class Pick(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val options: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavourite: Boolean = false
)

@Entity(tableName = "pick_history")
data class PickHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pickId: Long,
    val selectedOption: String,
    val timestamp: Long = System.currentTimeMillis()
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split('\u001F')
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        if (list == null) return ""
        return list.joinToString("\u001F")
    }
}
