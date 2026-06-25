package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.model.Design
import com.example.data.model.DesignElement
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object DesignExporter {

    // Render design to an offline Android Bitmap (HD support via scale)
    fun renderToBitmap(
        context: Context,
        design: Design,
        elements: List<DesignElement>,
        scale: Float = 1.5f
    ): Bitmap {
        val width = (design.width * scale).toInt()
        val height = (design.height * scale).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Draw Background
        when (design.backgroundType) {
            "SOLID" -> {
                canvas.drawColor(design.backgroundColor)
            }
            "GRADIENT" -> {
                val gradient = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    design.backgroundGradientStart, design.backgroundGradientEnd,
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
            }
            "IMAGE" -> {
                if (!design.backgroundImageUri.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(design.backgroundImageUri)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bgBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        if (bgBitmap != null) {
                            val srcRect = android.graphics.Rect(0, 0, bgBitmap.width, bgBitmap.height)
                            val destRect = android.graphics.Rect(0, 0, width, height)
                            canvas.drawBitmap(bgBitmap, srcRect, destRect, paint)
                            bgBitmap.recycle()
                        } else {
                            canvas.drawColor(Color.WHITE)
                        }
                    } catch (e: Exception) {
                        canvas.drawColor(Color.WHITE)
                    }
                } else {
                    canvas.drawColor(Color.WHITE)
                }
            }
            else -> canvas.drawColor(Color.WHITE)
        }

        // 2. Draw Elements in list order (bottom to top)
        for (element in elements) {
            val elX = element.x * scale
            val elY = element.y * scale
            val elW = element.width * scale
            val elH = element.height * scale

            canvas.save()
            // Apply translation & rotation around the center of the element
            val centerX = elX + elW / 2
            val centerY = elY + elH / 2
            canvas.rotate(element.rotation, centerX, centerY)

            when (element.type) {
                "TEXT" -> {
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = element.textColor
                        textSize = element.fontSize * scale
                        style = Paint.Style.FILL
                        
                        // Font pairing support
                        typeface = when (element.fontFamily) {
                            "Serif" -> Typeface.create(Typeface.SERIF, if (element.isBold) Typeface.BOLD else Typeface.NORMAL)
                            "Monospace" -> Typeface.create(Typeface.MONOSPACE, if (element.isBold) Typeface.BOLD else Typeface.NORMAL)
                            "Cursive" -> Typeface.create("sans-serif-condensed", if (element.isBold) Typeface.BOLD else Typeface.NORMAL)
                            else -> Typeface.create(Typeface.SANS_SERIF, if (element.isBold) Typeface.BOLD else Typeface.NORMAL)
                        }

                        if (element.isItalic) {
                            textSkewX = -0.25f
                        }
                        if (element.isUnderline) {
                            isUnderlineText = true
                        }
                    }

                    // Shadow effect
                    if (element.shadowRadius > 0f) {
                        textPaint.setShadowLayer(
                            element.shadowRadius * scale,
                            2f * scale,
                            2f * scale,
                            element.shadowColor
                        )
                    }

                    // Stroke effect
                    var strokePaint: Paint? = null
                    if (element.strokeWidth > 0f && element.strokeColor != 0) {
                        strokePaint = Paint(textPaint).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = element.strokeWidth * scale
                            color = element.strokeColor
                            setShadowLayer(0f, 0f, 0f, 0) // No shadow on stroke
                        }
                    }

                    // Break text into lines
                    val lines = element.text.split("\n")
                    val fm = textPaint.fontMetrics
                    val lineHeight = fm.descent - fm.ascent + fm.leading
                    var currentY = centerY - (lineHeight * lines.size) / 2f - fm.ascent

                    for (line in lines) {
                        val textWidth = textPaint.measureText(line)
                        val drawX = when (element.alignment) {
                            "LEFT" -> elX
                            "RIGHT" -> elX + elW - textWidth
                            else -> centerX - textWidth / 2f
                        }
                        
                        // Draw stroke first if exists
                        strokePaint?.let {
                            canvas.drawText(line, drawX, currentY, it)
                        }
                        canvas.drawText(line, drawX, currentY, textPaint)
                        currentY += lineHeight
                    }
                }

                "SHAPE" -> {
                    paint.reset()
                    paint.isAntiAlias = true
                    
                    // Style
                    if (element.fillColor != 0) {
                        paint.style = Paint.Style.FILL
                        paint.color = element.fillColor
                        
                        when (element.shapeType) {
                            "CIRCLE" -> canvas.drawOval(RectF(elX, elY, elX + elW, elY + elH), paint)
                            "SQUARE" -> canvas.drawRect(RectF(elX, elY, elX + elW, elY + elH), paint)
                            "ARROW" -> drawArrowOnCanvas(canvas, elX, elY, elX + elW, elY + elH, paint)
                            "LINE" -> {
                                paint.style = Paint.Style.STROKE
                                paint.strokeWidth = 6f * scale
                                canvas.drawLine(elX, centerY, elX + elW, centerY, paint)
                            }
                        }
                    }

                    // Shape Stroke
                    if (element.shapeStrokeWidth > 0f) {
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = element.shapeStrokeWidth * scale
                        paint.color = element.shapeStrokeColor
                        
                        when (element.shapeType) {
                            "CIRCLE" -> canvas.drawOval(RectF(elX, elY, elX + elW, elY + elH), paint)
                            "SQUARE" -> canvas.drawRect(RectF(elX, elY, elX + elW, elY + elH), paint)
                            "ARROW" -> drawArrowOnCanvas(canvas, elX, elY, elX + elW, elY + elH, paint)
                            "LINE" -> canvas.drawLine(elX, centerY, elX + elW, centerY, paint)
                        }
                    }
                }

                "STICKER" -> {
                    paint.reset()
                    paint.isAntiAlias = true
                    paint.color = element.fillColor
                    paint.style = Paint.Style.FILL

                    val stickerPath = getStickerPath(element.stickerName, elX, elY, elW, elH)
                    canvas.drawPath(stickerPath, paint)
                }

                "IMAGE" -> {
                    if (element.imageUri.isNotEmpty()) {
                        try {
                            val uri = Uri.parse(element.imageUri)
                            val inputStream = context.contentResolver.openInputStream(uri)
                            var rawBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                            if (rawBitmap != null) {
                                // 1. Apply background remover if needed (offline tolerance masking)
                                if (element.removeBgColorRange != null) {
                                    val procBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true)
                                    val targetColor = element.removeBgColorRange
                                    val targetR = Color.red(targetColor)
                                    val targetG = Color.green(targetColor)
                                    val targetB = Color.blue(targetColor)
                                    val threshold = (element.removeBgTolerance * 255).toInt()
                                    
                                    val pixels = IntArray(procBitmap.width * procBitmap.height)
                                    procBitmap.getPixels(pixels, 0, procBitmap.width, 0, 0, procBitmap.width, procBitmap.height)
                                    for (i in pixels.indices) {
                                        val p = pixels[i]
                                        val r = Color.red(p)
                                        val g = Color.green(p)
                                        val b = Color.blue(p)
                                        if (Math.abs(r - targetR) < threshold &&
                                            Math.abs(g - targetG) < threshold &&
                                            Math.abs(b - targetB) < threshold) {
                                            pixels[i] = Color.TRANSPARENT
                                        }
                                    }
                                    procBitmap.setPixels(pixels, 0, procBitmap.width, 0, 0, procBitmap.width, procBitmap.height)
                                    rawBitmap.recycle()
                                    rawBitmap = procBitmap
                                }

                                // 2. Crop selection
                                val srcLeft = (element.cropLeft * rawBitmap.width).toInt()
                                val srcTop = (element.cropTop * rawBitmap.height).toInt()
                                val srcRight = (element.cropRight * rawBitmap.width).toInt()
                                val srcBottom = (element.cropBottom * rawBitmap.height).toInt()
                                
                                val srcRect = android.graphics.Rect(
                                    Math.max(0, srcLeft),
                                    Math.max(0, srcTop),
                                    Math.min(rawBitmap.width, srcRight),
                                    Math.min(rawBitmap.height, srcBottom)
                                )
                                val destRect = RectF(elX, elY, elX + elW, elY + elH)
                                
                                canvas.drawBitmap(rawBitmap, srcRect, destRect, paint)
                                rawBitmap.recycle()
                            }
                        } catch (e: Exception) {
                            // Fallback outline if loading failed
                            paint.style = Paint.Style.STROKE
                            paint.color = Color.RED
                            paint.strokeWidth = 2f * scale
                            canvas.drawRect(RectF(elX, elY, elX + elW, elY + elH), paint)
                        }
                    }
                }
            }

            canvas.restore()
        }

        return bitmap
    }

    private fun drawArrowOnCanvas(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, paint: Paint) {
        val path = Path()
        // Draw main line
        canvas.drawLine(x1, y1 + (y2 - y1) / 2, x2 - 20, y1 + (y2 - y1) / 2, paint)
        
        // Draw head
        val midY = y1 + (y2 - y1) / 2
        path.moveTo(x2, midY)
        path.lineTo(x2 - 30, midY - 20)
        path.lineTo(x2 - 30, midY + 20)
        path.close()
        
        val prevStyle = paint.style
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        paint.style = prevStyle
    }

    private fun getStickerPath(name: String, x: Float, y: Float, w: Float, h: Float): Path {
        val path = Path()
        when (name) {
            "Star" -> {
                val cx = x + w / 2
                val cy = y + h / 2
                val spikes = 5
                val outerRadius = w / 2
                val innerRadius = w / 5
                var rot = Math.PI / 2 * 3
                val step = Math.PI / spikes

                path.moveTo(cx, cy - outerRadius)
                for (i in 0 until spikes) {
                    var sx = cx + Math.cos(rot).toFloat() * outerRadius
                    var sy = cy + Math.sin(rot).toFloat() * outerRadius
                    path.lineTo(sx, sy)
                    rot += step

                    sx = cx + Math.cos(rot).toFloat() * innerRadius
                    sy = cy + Math.sin(rot).toFloat() * innerRadius
                    path.lineTo(sx, sy)
                    rot += step
                }
                path.close()
            }
            "Heart" -> {
                path.moveTo(x + w / 2f, y + h * 0.25f)
                path.cubicTo(
                    x + w * 0.1f, y + h * 0.05f, 
                    x + w * 0.01f, y + h * 0.5f, 
                    x + w / 2f, y + h * 0.9f
                )
                path.cubicTo(
                    x + w * 0.99f, y + h * 0.5f, 
                    x + w * 0.9f, y + h * 0.05f, 
                    x + w / 2f, y + h * 0.25f
                )
                path.close()
            }
            "Sparkle" -> {
                path.moveTo(x + w / 2f, y)
                path.quadTo(x + w / 2f, y + h / 2f, x + w, y + h / 2f)
                path.quadTo(x + w / 2f, y + h / 2f, x + w / 2f, y + h)
                path.quadTo(x + w / 2f, y + h / 2f, x, y + h / 2f)
                path.quadTo(x + w / 2f, y + h / 2f, x + w / 2f, y)
                path.close()
            }
            "Lightning" -> {
                path.moveTo(x + w * 0.6f, y)
                path.lineTo(x + w * 0.15f, y + h * 0.55f)
                path.lineTo(x + w * 0.5f, y + h * 0.55f)
                path.lineTo(x + w * 0.4f, y + h)
                path.lineTo(x + w * 0.85f, y + h * 0.45f)
                path.lineTo(x + w * 0.5f, y + h * 0.45f)
                path.close()
            }
            "Fire" -> {
                path.moveTo(x + w * 0.5f, y + h)
                path.cubicTo(x + w * 0.1f, y + h * 0.85f, x + w * 0.15f, y + h * 0.45f, x + w * 0.5f, y + h * 0.2f)
                path.cubicTo(x + w * 0.35f, y + h * 0.4f, x + w * 0.45f, y + h * 0.55f, x + w * 0.5f, y + h * 0.55f)
                path.cubicTo(x + w * 0.6f, y + h * 0.3f, x + w * 0.55f, y + h * 0.1f, x + w * 0.75f, y)
                path.cubicTo(x + w * 0.95f, y + h * 0.45f, x + w * 0.9f, y + h * 0.85f, x + w * 0.5f, y + h)
                path.close()
            }
            "Checkmark" -> {
                path.moveTo(x + w * 0.1f, y + h * 0.55f)
                path.lineTo(x + w * 0.4f, y + h * 0.85f)
                path.lineTo(x + w * 0.95f, y + h * 0.15f)
                path.lineTo(x + w * 0.85f, y + h * 0.05f)
                path.lineTo(x + w * 0.4f, y + h * 0.65f)
                path.lineTo(x + w * 0.2f, y + h * 0.45f)
                path.close()
            }
            "Sale Tag" -> {
                path.moveTo(x + w * 0.15f, y + h * 0.15f)
                path.lineTo(x + w * 0.65f, y + h * 0.15f)
                path.lineTo(x + w * 0.95f, y + h * 0.45f)
                path.lineTo(x + w * 0.65f, y + h * 0.95f)
                path.lineTo(x + w * 0.15f, y + h * 0.95f)
                path.close()
            }
            "Promo Banner" -> {
                path.moveTo(x, y + h * 0.2f)
                path.lineTo(x + w, y + h * 0.2f)
                path.lineTo(x + w * 0.9f, y + h * 0.5f)
                path.lineTo(x + w, y + h * 0.8f)
                path.lineTo(x, y + h * 0.8f)
                path.lineTo(x + w * 0.1f, y + h * 0.5f)
                path.close()
            }
            else -> {
                path.addOval(RectF(x, y, x + w, y + h), Path.Direction.CW)
            }
        }
        return path
    }

    // Export to MediaStore as PNG or JPG offline and toast confirmation
    fun exportToGallery(
        context: Context,
        design: Design,
        elements: List<DesignElement>,
        format: String, // "PNG" or "JPG"
        scale: Float = 2.0f // HD scale
    ): Uri? {
        val bitmap = renderToBitmap(context, design, elements, scale)
        val fileName = "Designo_${design.name.replace(" ", "_")}_${System.currentTimeMillis()}"
        val mimeType = if (format == "PNG") "image/png" else "image/jpeg"
        val compressFormat = if (format == "PNG") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

        var uri: Uri? = null
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.${format.lowercase()}")
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Designo")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(imageUri)
                    if (out != null) {
                        bitmap.compress(compressFormat, 100, out)
                        out.close()
                        uri = imageUri
                    }
                }
            } else {
                val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Designo")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, "$fileName.${format.lowercase()}")
                val out = FileOutputStream(file)
                bitmap.compress(compressFormat, 100, out)
                out.close()
                uri = Uri.fromFile(file)
            }
            Toast.makeText(context, "Successfully saved to Photos/Gallery!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            bitmap.recycle()
        }
        return uri
    }

    // Export Design to PDF offline and toast confirmation
    fun exportToPDF(
        context: Context,
        design: Design,
        elements: List<DesignElement>
    ): Uri? {
        val scale = 1.0f // Standard scale for PDF pages
        val bitmap = renderToBitmap(context, design, elements, scale)
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        pdfDocument.finishPage(page)

        val fileName = "Designo_${design.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        var pdfUri: Uri? = null
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Designo")
                }
                val docUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (docUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(docUri)
                    if (out != null) {
                        pdfDocument.writeTo(out)
                        out.close()
                        pdfUri = docUri
                    }
                }
            } else {
                val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Designo")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                val out = FileOutputStream(file)
                pdfDocument.writeTo(out)
                out.close()
                pdfUri = Uri.fromFile(file)
            }
            Toast.makeText(context, "Successfully saved to Downloads/Designo folder!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF Export error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
            bitmap.recycle()
        }
        return pdfUri
    }
}
