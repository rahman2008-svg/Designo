package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "designs")
data class Design(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Instagram Post", "Facebook Post", "YouTube Thumbnail", "Business Flyer", "Poster", "CV Design", "Business Card", "Logo", "Social Media Kit"
    val width: Float,
    val height: Float,
    val backgroundType: String = "SOLID", // "SOLID", "GRADIENT", "IMAGE"
    val backgroundColor: Int = -1, // Solid white by default
    val backgroundGradientStart: Int = -0xff0001, // Indigo
    val backgroundGradientEnd: Int = -0xff01, // Cyan
    val backgroundImageUri: String? = null,
    val elementsJson: String = "[]", // Serialized List<DesignElement>
    val updatedAt: Long = System.currentTimeMillis()
)
