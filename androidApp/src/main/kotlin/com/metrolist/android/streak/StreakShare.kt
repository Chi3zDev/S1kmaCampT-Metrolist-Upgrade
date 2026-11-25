package com.metrolist.android.streak

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
    // An original vector flame, rendered locally without external assets.
    val flame =
        Path().apply {
            moveTo(540f, top + 72f)
            cubicTo(475f, top + 167f, 430f, top + 205f, 430f, top + 270f)
            cubicTo(430f, top + 340f, 475f, top + 382f, 540f, top + 382f)
            cubicTo(605f, top + 382f, 650f, top + 340f, 650f, top + 270f)
            cubicTo(650f, top + 210f, 612f, top + 160f, 602f, top + 144f)
            cubicTo(598f, top + 186f, 579f, top + 200f, 568f, top + 205f)
            cubicTo(580f, top + 160f, 558f, top + 106f, 540f, top + 72f)
            close()
        }
    paint.color = accent
    canvas.drawPath(flame, paint)
    paint.color = Color.argb(180, 255, 255, 255)
    canvas.drawCircle(540f, top + 320f, 31f, paint)
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
