package com.metrolist.android.streak

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb

/** Only aggregate streak information and weekday labels are rendered; no account or listening history. */
internal fun renderStreakShare(
    context: Context,
    current: Int,
    longestLabel: String,
    level: String,
    daysLabel: String,
    weekLabel: String,
    weekdays: List<Pair<String, Boolean>>,
    accent: Int,
    variant: Int,
    extinguished: Boolean,
    background: Int,
    foreground: Int,
    story: Boolean,
): ImageBitmap {
    require(weekdays.size == 7)
    val bitmap = Bitmap.createBitmap(1080, if (story) 1920 else 1080, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(background)
    val top = if (story) 390f else 50f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun label(
        value: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        centered: Boolean = true,
    ) {
        paint.color = color
        paint.textSize = size
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = if (centered) Paint.Align.CENTER else Paint.Align.LEFT
        val text = TextUtils.ellipsize(value, TextPaint(paint), 960f, TextUtils.TruncateAt.END).toString()
        canvas.drawText(text, x, y, paint)
    }
    // Reuse the exact supplied vector paths used by the live flame.
    val artwork = loadFlameArtwork(context)[variant.coerceIn(0, 6)]
    val factor = 380f / 180f
    canvas.save()
    canvas.translate(350f + (380f - 140f * factor) / 2f + 10f * factor, top + 40f + 40f * factor)
    canvas.scale(factor, factor)
    if (extinguished) {
        paint.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        paint.alpha = 153
    }
    artwork.layers.forEach { layer ->
        val bounds = layer.path.getBounds()
        paint.shader = LinearGradient(0f, bounds.top, 0f, bounds.bottom, layer.top.toArgb(), layer.bottom.toArgb(), Shader.TileMode.CLAMP)
        canvas.drawPath(layer.path.asAndroidPath(), paint)
    }
    paint.shader = null
    paint.color = Color.WHITE
    paint.alpha = if (extinguished) 43 else (artwork.highlightAlpha * 255).toInt()
    canvas.drawPath(artwork.highlight.asAndroidPath(), paint)
    paint.alpha = 255
    paint.colorFilter = null
    canvas.restore()
    label(current.toString(), 540f, top + 510f, 112f, foreground)
    label(daysLabel, 540f, top + 560f, 38f, foreground)
    label(level, 540f, top + 630f, 52f, accent)
    label(weekLabel, 540f, top + 716f, 34f, foreground)
    weekdays.forEachIndexed { index, (day, counted) ->
        val x = 180f + index * 120f
        label(day, x, top + 775f, 28f, foreground)
        paint.color = if (counted) accent else foreground
        paint.alpha = if (counted) 255 else 80
        canvas.drawCircle(x, top + 813f, 17f, paint)
        paint.alpha = 255
    }
    label(longestLabel, 540f, top + 900f, 36f, foreground)
    label("Metrolist", 540f, top + 980f, 32f, foreground)
    return bitmap.asImageBitmap()
}
