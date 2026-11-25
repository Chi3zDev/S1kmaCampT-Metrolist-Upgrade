package com.metrolist.android.streak

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Only aggregate streak information and weekday labels are rendered; no account or listening history. */
internal fun renderStreakShare(
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
    // Use the same original normalized contour as the live flame.
    val geometry = flameShape(variant)
    val left = 350f
    val flameTop = top + 40f
    val side = 380f

    fun x(value: Float) = left + side * value

    fun y(value: Float) = flameTop + side * value
    val flame =
        Path().apply {
            moveTo(x(geometry.tipX), y(geometry.tipY))
            cubicTo(x(.42f), y(.25f), x(geometry.notchX), y(.27f), x(geometry.notchX), y(geometry.notchY))
            cubicTo(x(.70f), y(.46f), x(geometry.rightX), y(.39f), x(geometry.rightX), y(geometry.rightY))
            cubicTo(x(.93f), y(.60f), x(.84f), y(.91f), x(.56f), y(.93f))
            cubicTo(x(.23f), y(.97f), x(.12f), y(.76f), x(.24f), y(.53f))
            cubicTo(x(geometry.leftControlX), y(.40f), x(.40f), y(.30f), x(geometry.tipX), y(geometry.tipY))
            close()
        }
    val tip = if (variant < 4) Color.rgb(255, 72, 182) else Color.rgb(207, 185, 255)
    if (extinguished) {
        paint.color = accent
    } else {
        paint.shader = LinearGradient(0f, y(.08f), 0f, y(.94f), tip, accent, Shader.TileMode.CLAMP)
    }
    canvas.drawPath(flame, paint)
    paint.shader = null
    val inner =
        Path().apply {
            moveTo(x(.50f), y(geometry.middleTipY))
            cubicTo(x(.38f), y(.48f), x(.27f), y(.64f), x(.32f), y(.77f))
            cubicTo(x(.37f), y(.91f), x(.66f), y(.91f), x(.70f), y(.76f))
            cubicTo(x(.73f), y(.65f), x(.66f), y(.53f), x(.61f), y(geometry.middleNotchY))
            cubicTo(x(.63f), y(.62f), x(.55f), y(.60f), x(.50f), y(geometry.middleTipY))
            close()
        }
    if (extinguished) {
        paint.color = accent
    } else {
        paint.shader = LinearGradient(0f, y(.32f), 0f, y(.91f), accent, Color.WHITE, Shader.TileMode.CLAMP)
    }
    canvas.drawPath(inner, paint)
    paint.shader = null
    val core =
        Path().apply {
            moveTo(x(.51f), y(geometry.coreTipY))
            cubicTo(x(.42f), y(.69f), x(.40f), y(.84f), x(.51f), y(.87f))
            cubicTo(x(.62f), y(.85f), x(.63f), y(.74f), x(.51f), y(geometry.coreTipY))
            close()
        }
    if (!extinguished) {
        paint.color = Color.WHITE
        canvas.drawPath(core, paint)
    }
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
