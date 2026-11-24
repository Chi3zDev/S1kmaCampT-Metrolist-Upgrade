package com.metrolist.android.recap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun renderRecapShare(
    recap: RecapData,
    title: String,
    minutesLabel: String,
    songsLabel: String,
    artistsLabel: String,
    background: Int,
    foreground: Int,
    accent: Int,
    story: Boolean,
): ImageBitmap {
    val width = 1080
    val height = if (story) 1920 else 1080
    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(image)
    canvas.drawColor(background)
    val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = foreground
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    val top = if (story) 330f else 100f

    fun text(
        value: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        maxWidth: Float = 900f,
    ) {
        paint.textSize = size
        paint.color = color
        canvas.drawText(
            TextUtils.ellipsize(value, android.text.TextPaint(paint), maxWidth, TextUtils.TruncateAt.END).toString(),
            x,
            y,
            paint,
        )
    }
    text(title, 90f, top, 56f, foreground)
    text(recap.minutes.toString(), 90f, top + 130f, 116f, accent)
    text(minutesLabel, 90f, top + 190f, 38f, foreground)
    text(songsLabel, 90f, top + 300f, 42f, accent)
    recap.songs.take(3).forEachIndexed { i, song -> text("${i + 1}. ${song.title}", 90f, top + 360f + i * 55f, 34f, foreground) }
    text(artistsLabel, 90f, top + 585f, 42f, accent)
    recap.artists.take(3).forEachIndexed { i, artist -> text("${i + 1}. ${artist.name}", 90f, top + 645f + i * 55f, 34f, foreground) }
    return image.asImageBitmap()
}
