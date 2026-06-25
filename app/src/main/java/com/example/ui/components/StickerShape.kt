package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun StickerShape(
    stickerName: String,
    color: Color,
    modifier: Modifier = Modifier,
    isStrokeOnly: Boolean = false,
    strokeWidth: Float = 5f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path()

        when (stickerName) {
            "Star" -> {
                // 5-pointed star math
                val cx = w / 2
                val cy = h / 2
                val spikes = 5
                val outerRadius = w / 2
                val innerRadius = w / 5
                
                var rot = Math.PI / 2 * 3
                var x = cx
                var y = cy
                val step = Math.PI / spikes

                path.moveTo(cx, cy - outerRadius)
                for (i in 0 until spikes) {
                    x = cx + Math.cos(rot).toFloat() * outerRadius
                    y = cy + Math.sin(rot).toFloat() * outerRadius
                    path.lineTo(x, y)
                    rot += step

                    x = cx + Math.cos(rot).toFloat() * innerRadius
                    y = cy + Math.sin(rot).toFloat() * innerRadius
                    path.lineTo(x, y)
                    rot += step
                }
                path.close()
            }
            "Heart" -> {
                // Classic bezier heart path
                path.moveTo(w / 2f, h * 0.25f)
                // Left hump
                path.cubicTo(
                    w * 0.1f, h * 0.05f, 
                    w * 0.01f, h * 0.5f, 
                    w / 2f, h * 0.9f
                )
                // Right hump
                path.cubicTo(
                    w * 0.99f, h * 0.5f, 
                    w * 0.9f, h * 0.05f, 
                    w / 2f, h * 0.25f
                )
                path.close()
            }
            "Sparkle" -> {
                // 4-pointed sparkle star with curved inward edges
                path.moveTo(w / 2f, 0f)
                path.quadraticTo(w / 2f, h / 2f, w, h / 2f)
                path.quadraticTo(w / 2f, h / 2f, w / 2f, h)
                path.quadraticTo(w / 2f, h / 2f, 0f, h / 2f)
                path.quadraticTo(w / 2f, h / 2f, w / 2f, 0f)
                path.close()
            }
            "Lightning" -> {
                // Lightning bolt
                path.moveTo(w * 0.6f, 0f)
                path.lineTo(w * 0.15f, h * 0.55f)
                path.lineTo(w * 0.5f, h * 0.55f)
                path.lineTo(w * 0.4f, h)
                path.lineTo(w * 0.85f, h * 0.45f)
                path.lineTo(w * 0.5f, h * 0.45f)
                path.close()
            }
            "Fire" -> {
                // Flame curves
                path.moveTo(w * 0.5f, h)
                path.cubicTo(w * 0.1f, h * 0.85f, w * 0.15f, h * 0.45f, w * 0.5f, h * 0.2f)
                path.cubicTo(w * 0.35f, h * 0.4f, w * 0.45f, h * 0.55f, w * 0.5f, h * 0.55f)
                path.cubicTo(w * 0.6f, h * 0.3f, w * 0.55f, h * 0.1f, w * 0.75f, 0f)
                path.cubicTo(w * 0.95f, h * 0.45f, w * 0.9f, h * 0.85f, w * 0.5f, h)
                path.close()
            }
            "Checkmark" -> {
                // Nice tick checkmark
                path.moveTo(w * 0.1f, h * 0.55f)
                path.lineTo(w * 0.4f, h * 0.85f)
                path.lineTo(w * 0.95f, h * 0.15f)
                path.lineTo(w * 0.85f, h * 0.05f)
                path.lineTo(w * 0.4f, h * 0.65f)
                path.lineTo(w * 0.2f, h * 0.45f)
                path.close()
            }
            "Sale Tag" -> {
                // Tag shape
                path.moveTo(w * 0.15f, h * 0.15f)
                path.lineTo(w * 0.65f, h * 0.15f)
                path.lineTo(w * 0.95f, h * 0.45f)
                path.lineTo(w * 0.65f, h * 0.95f)
                path.lineTo(w * 0.15f, h * 0.95f)
                path.close()
            }
            "Promo Banner" -> {
                // Ribbon banner
                path.moveTo(0f, h * 0.2f)
                path.lineTo(w, h * 0.2f)
                path.lineTo(w * 0.9f, h * 0.5f)
                path.lineTo(w, h * 0.8f)
                path.lineTo(0f, h * 0.8f)
                path.lineTo(w * 0.1f, h * 0.5f)
                path.close()
            }
            "Circle Badge" -> {
                // Classic circle badge with border or scalloped fill
                path.addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
            }
            else -> {
                // Standard Box
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w, h)
                path.lineTo(0f, h)
                path.close()
            }
        }

        if (isStrokeOnly) {
            drawPath(path, color, style = Stroke(width = strokeWidth))
        } else {
            drawPath(path, color, style = Fill)
        }
    }
}
