package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DesignElement(
    val id: String,
    val type: String, // "TEXT", "SHAPE", "STICKER", "IMAGE"
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    val isLocked: Boolean = false,
    
    // Text specific properties
    val text: String = "",
    val fontSize: Float = 24f,
    val textColor: Int = -1, // White
    val fontFamily: String = "SansSerif", // "SansSerif", "Serif", "Monospace", "Cursive"
    val alignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val shadowColor: Int = 0x80000000.toInt(),
    val shadowRadius: Float = 0f,
    val strokeColor: Int = 0,
    val strokeWidth: Float = 0f,
    
    // Shape specific properties
    val shapeType: String = "SQUARE", // "CIRCLE", "SQUARE", "ARROW", "LINE"
    val fillColor: Int = -0x10000, // Red
    val shapeStrokeColor: Int = -1,
    val shapeStrokeWidth: Float = 0f,
    
    // Sticker specific properties
    val stickerName: String = "", // e.g. "star", "heart", "sparkle"
    
    // Image specific properties
    val imageUri: String = "",
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val removeBgColorRange: Int? = null, // Color selection for basic offline background removal
    val removeBgTolerance: Float = 0.1f
)
