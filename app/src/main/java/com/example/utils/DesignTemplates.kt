package com.example.utils

import androidx.compose.ui.graphics.Color
import com.example.data.model.DesignElement
import java.util.UUID

// Modern Color Palettes for our palette generator
data class ColorPalette(
    val name: String,
    val primary: Int,
    val secondary: Int,
    val accent: Int,
    val background: Int
)

// Predefined Font Pairings
data class FontPairing(
    val name: String,
    val titleFamily: String,
    val bodyFamily: String,
    val description: String
)

object DesignTemplates {

    val palettes = listOf(
        ColorPalette("Cyberpunk Neon", 0xFF00F0FF.toInt(), 0xFFFF007F.toInt(), 0xFF9D00FF.toInt(), 0xFF0F0C1B.toInt()),
        ColorPalette("Retro Sunset", 0xFFFF5F6D.toInt(), 0xFFFFC371.toInt(), 0xFF4A0E17.toInt(), 0xFFFAF3F0.toInt()),
        ColorPalette("Forest Mist", 0xFF2C5E43.toInt(), 0xFF8FA89B.toInt(), 0xFFD2B48C.toInt(), 0xFFF0F5F2.toInt()),
        ColorPalette("Royal Gold", 0xFF1E293B.toInt(), 0xFFD4AF37.toInt(), 0xFFF8FAFC.toInt(), 0xFF0F172A.toInt()),
        ColorPalette("Nordic Blue", 0xFF4C6EF5.toInt(), 0xFF22B8CF.toInt(), 0xFFFAB005.toInt(), 0xFFF8F9FA.toInt()),
        ColorPalette("Sweet Pastel", 0xFFFFD3B6.toInt(), 0xFFFFAAA6.toInt(), 0xFFFF8B94.toInt(), 0xFFFFF9F9.toInt()),
        ColorPalette("Sleek Dark", 0xFFE2E8F0.toInt(), 0xFF94A3B8.toInt(), 0xFF38BDF8.toInt(), 0xFF0F172A.toInt()),
        ColorPalette("Brutalist High-Contrast", 0xFFFFFF00.toInt(), 0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt())
    )

    val fontPairings = listOf(
        FontPairing("Tech Bold", "SansSerif", "Monospace", "High-energy sans paired with tech-focused mono, great for thumbnails."),
        FontPairing("Classic Elegant", "Serif", "SansSerif", "Sophisticated editorial look for CVs and business cards."),
        FontPairing("Playful Warm", "Cursive", "SansSerif", "Fun and organic style for social media posts and flyers."),
        FontPairing("Balanced Modern", "SansSerif", "SansSerif", "Clean, highly readable, and professional for any layout.")
    )

    val stickers = listOf(
        "Star", "Heart", "Sparkle", "Sale Tag", "Promo Banner", "Checkmark", "Lightning", "Fire", "Circle Badge"
    )

    fun getRandomPalette(): ColorPalette {
        return palettes.random()
    }

    // Standard Preset Categories and their Canvas Dimensions
    val categories = listOf(
        CategoryPreset("Instagram Post", "Square social banner", 1080f, 1080f, "🎨"),
        CategoryPreset("Facebook Post", "Landscape social feed", 1200f, 630f, "📱"),
        CategoryPreset("YouTube Thumbnail", "Video teaser graphic", 1280f, 720f, "📺"),
        CategoryPreset("Poster", "Vertical wall poster", 800f, 1200f, "🖼️"),
        CategoryPreset("Business Flyer", "Marketing handout", 816f, 1056f, "📄"),
        CategoryPreset("CV Design", "Professional resume", 800f, 1130f, "👔"),
        CategoryPreset("Business Card", "Networking card", 1050f, 600f, "📇"),
        CategoryPreset("Logo Design", "Brand identity icon", 1000f, 1000f, "💎"),
        CategoryPreset("Social Media Kit", "Multi-use banner", 1200f, 1200f, "🚀")
    )

    data class CategoryPreset(
        val name: String,
        val description: String,
        val width: Float,
        val height: Float,
        val icon: String
    )

    // Generate elements for a template based on category and style selection
    fun createTemplateElements(category: String, width: Float, height: Float, palette: ColorPalette): List<DesignElement> {
        val list = mutableListOf<DesignElement>()
        
        when (category) {
            "YouTube Thumbnail" -> {
                // Background shape (Dark accent block)
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = 0f, y = height * 0.7f,
                        width = width, height = height * 0.3f,
                        shapeType = "SQUARE",
                        fillColor = palette.secondary
                    )
                )
                // Accent visual element (Circle background)
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = width * 0.7f, y = height * 0.1f,
                        width = 300f, height = 300f,
                        shapeType = "CIRCLE",
                        fillColor = palette.accent
                    )
                )
                // Text Title - Bold Sans
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = height * 0.15f,
                        width = width * 0.65f, height = 200f,
                        text = "CRACK THE\nCODE!",
                        fontSize = 55f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "LEFT",
                        shadowColor = 0xFF000000.toInt(),
                        shadowRadius = 8f
                    )
                )
                // Text Subtitle
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = height * 0.45f,
                        width = width * 0.65f, height = 80f,
                        text = "100% Offline App Development",
                        fontSize = 24f,
                        textColor = 0xFFFFFFFF.toInt(),
                        fontFamily = "Monospace",
                        alignment = "LEFT"
                    )
                )
                // Custom Sticker
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "STICKER",
                        x = width * 0.72f, y = height * 0.15f,
                        width = 180f, height = 180f,
                        stickerName = "Sparkle",
                        fillColor = palette.primary
                    )
                )
            }
            "Instagram Post" -> {
                // Solid border
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = 40f, y = 40f,
                        width = width - 80f, height = height - 80f,
                        shapeType = "SQUARE",
                        fillColor = 0x00000000, // Transparent fill
                        shapeStrokeColor = palette.primary,
                        shapeStrokeWidth = 8f
                    )
                )
                // Large typography
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height * 0.25f,
                        width = width - 200f, height = 150f,
                        text = "CREATIVE VIBES",
                        fontSize = 42f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "CENTER"
                    )
                )
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height * 0.42f,
                        width = width - 200f, height = 100f,
                        text = "Offline Design Studio",
                        fontSize = 24f,
                        textColor = palette.secondary,
                        fontFamily = "Serif",
                        isItalic = true,
                        alignment = "CENTER"
                    )
                )
                // Shape accent
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = width / 2f - 60f, y = height * 0.58f,
                        width = 120f, height = 6f,
                        shapeType = "SQUARE",
                        fillColor = palette.accent
                    )
                )
                // Interactive Sticker
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "STICKER",
                        x = width / 2f - 50f, y = height * 0.68f,
                        width = 100f, height = 100f,
                        stickerName = "Heart"
                    )
                )
            }
            "CV Design" -> {
                // Header accent block
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = 0f, y = 0f,
                        width = width, height = 220f,
                        shapeType = "SQUARE",
                        fillColor = palette.primary
                    )
                )
                // Applicant Name
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = 40f,
                        width = width - 100f, height = 80f,
                        text = "PRINCE AR ABDUR RAHMAN",
                        fontSize = 32f,
                        textColor = 0xFFFFFFFF.toInt(),
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "LEFT"
                    )
                )
                // Professional Title
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = 120f,
                        width = width - 100f, height = 50f,
                        text = "Professional Android Developer | Designer",
                        fontSize = 18f,
                        textColor = palette.secondary,
                        fontFamily = "Monospace",
                        alignment = "LEFT"
                    )
                )
                // Experience Section Title
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = 260f,
                        width = width - 100f, height = 40f,
                        text = "WORK EXPERIENCE",
                        fontSize = 20f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "LEFT"
                    )
                )
                // Horizontal divider line
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = 50f, y = 305f,
                        width = width - 100f, height = 3f,
                        shapeType = "SQUARE",
                        fillColor = palette.primary
                    )
                )
                // Experience Text
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = 320f,
                        width = width - 100f, height = 180f,
                        text = "Lead App Developer - NexVora Lab's Ofc (2025 - Present)\n• Built the cutting-edge offline graphic editor 'Designo'.\n• Architected MVVM local systems with SQLite / Room Databases.\n• Handcrafted highly fluid and accessible Material 3 interfaces.",
                        fontSize = 15f,
                        textColor = 0xFF222222.toInt(),
                        fontFamily = "SansSerif",
                        alignment = "LEFT"
                    )
                )
                // Skills Section Title
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = 530f,
                        width = width - 100f, height = 40f,
                        text = "CORE EXPERTISE",
                        fontSize = 20f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "LEFT"
                    )
                )
                // Skill divider line
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = 50f, y = 575f,
                        width = width - 100f, height = 3f,
                        shapeType = "SQUARE",
                        fillColor = palette.primary
                    )
                )
                // Skills Text
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 50f, y = 590f,
                        width = width - 100f, height = 120f,
                        text = "• Kotlin, Compose, Android SDK, MediaStore APIs\n• High-performance Offline Canvas Engines & Geometry Snapping\n• SQLite Architecture, Local Filesystems, Custom Image Manipulation",
                        fontSize = 15f,
                        textColor = 0xFF222222.toInt(),
                        fontFamily = "SansSerif",
                        alignment = "LEFT"
                    )
                )
            }
            "Business Card" -> {
                // Elegant dark card with minimal layout
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = 20f, y = 20f,
                        width = width - 40f, height = height - 40f,
                        shapeType = "SQUARE",
                        fillColor = 0x00000000,
                        shapeStrokeColor = palette.accent,
                        shapeStrokeWidth = 4f
                    )
                )
                // Company Name
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 60f, y = 60f,
                        width = width - 120f, height = 60f,
                        text = "NEXVORA LAB'S OFC",
                        fontSize = 24f,
                        textColor = palette.accent,
                        fontFamily = "Monospace",
                        isBold = true,
                        alignment = "LEFT"
                    )
                )
                // Slogan
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 60f, y = 110f,
                        width = width - 120f, height = 40f,
                        text = "Next-Generation Digital Products",
                        fontSize = 12f,
                        textColor = 0x88FFFFFF.toInt(),
                        fontFamily = "SansSerif",
                        alignment = "LEFT"
                    )
                )
                // Representative Name
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 60f, y = height * 0.45f,
                        width = width - 120f, height = 60f,
                        text = "Prince AR Abdur Rahman",
                        fontSize = 28f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "LEFT"
                    )
                )
                // Professional Role
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 60f, y = height * 0.45f + 55f,
                        width = width - 120f, height = 40f,
                        text = "Independent App Developer",
                        fontSize = 14f,
                        textColor = palette.secondary,
                        fontFamily = "Serif",
                        isItalic = true,
                        alignment = "LEFT"
                    )
                )
                // Contact Info (Bottom Right)
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = width * 0.5f, y = height - 160f,
                        width = width * 0.45f, height = 100f,
                        text = "📞 WhatsApp: 01707424006\n✉️ prince@nexvora.com",
                        fontSize = 11f,
                        textColor = 0xFFEEEEEE.toInt(),
                        fontFamily = "Monospace",
                        alignment = "RIGHT"
                    )
                )
                // Little Diamond icon
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "STICKER",
                        x = width - 120f, y = 50f,
                        width = 60f, height = 60f,
                        stickerName = "Sparkle",
                        fillColor = palette.accent
                    )
                )
            }
            "Logo Design" -> {
                // Circle logo layout
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = width / 2f - 180f, y = height / 2f - 240f,
                        width = 360f, height = 360f,
                        shapeType = "CIRCLE",
                        fillColor = 0x0DFFFFFF, // Very subtle translucent circle
                        shapeStrokeColor = palette.primary,
                        shapeStrokeWidth = 10f
                    )
                )
                // Central Sticker
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "STICKER",
                        x = width / 2f - 100f, y = height / 2f - 160f,
                        width = 200f, height = 200f,
                        stickerName = "Sparkle",
                        fillColor = palette.secondary
                    )
                )
                // Logo Text Top
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height / 2f + 160f,
                        width = width - 200f, height = 70f,
                        text = "DESIGNO",
                        fontSize = 36f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "CENTER"
                    )
                )
                // Logo Slogan
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height / 2f + 230f,
                        width = width - 200f, height = 50f,
                        text = "NexVora Lab's Ofc",
                        fontSize = 14f,
                        textColor = palette.accent,
                        fontFamily = "Monospace",
                        alignment = "CENTER"
                    )
                )
            }
            else -> {
                // Default Poster/Flyer fallback
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height * 0.15f,
                        width = width - 200f, height = 150f,
                        text = "DESIGNO STUDIO",
                        fontSize = 44f,
                        textColor = palette.primary,
                        fontFamily = "SansSerif",
                        isBold = true,
                        alignment = "CENTER"
                    )
                )
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height * 0.3f,
                        width = width - 200f, height = 80f,
                        text = "FULLY OFFLINE DESIGN MAKER",
                        fontSize = 18f,
                        textColor = palette.secondary,
                        fontFamily = "Monospace",
                        alignment = "CENTER"
                    )
                )
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "SHAPE",
                        x = width / 2f - 200f, y = height * 0.45f,
                        width = 400f, height = 250f,
                        shapeType = "SQUARE",
                        fillColor = 0x0DFFFFFF,
                        shapeStrokeColor = palette.accent,
                        shapeStrokeWidth = 3f
                    )
                )
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "STICKER",
                        x = width / 2f - 75f, y = height * 0.45f + 50f,
                        width = 150f, height = 150f,
                        stickerName = "Star"
                    )
                )
                list.add(
                    DesignElement(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        x = 100f, y = height - 200f,
                        width = width - 200f, height = 80f,
                        text = "Create Posters, Flyers & Logo Designs",
                        fontSize = 16f,
                        textColor = 0xFF94A3B8.toInt(),
                        fontFamily = "SansSerif",
                        alignment = "CENTER"
                    )
                )
            }
        }
        
        return list
    }
}
