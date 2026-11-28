package com.metrolist.android.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.AudioManager
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.metrolist.shared.viewmodels.PlayerViewModel.SongUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

internal enum class MusicShareSurface { BLUR, CLEAR, FLAT, FROSTED }

internal data class MusicShareRequest(
    val song: SongUiModel,
    val positionMs: Long,
    val durationMs: Long,
    val playing: Boolean,
)

/** A purpose-built image, rather than a capture of AndroidView or video surfaces. */
internal suspend fun renderMusicSharePoster(
    context: Context,
    song: SongUiModel,
    positionMs: Long,
    durationMs: Long,
    playing: Boolean,
    surface: MusicShareSurface = MusicShareSurface.FLAT,
): ImageBitmap {
    val artwork =
        song.displayThumbnail?.let { url ->
            try {
                val request =
                    ImageRequest
                        .Builder(context)
                        .data(url)
                        .size(800)
                        .allowHardware(false)
                        .build()
                (context.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }
        }
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val volume = (audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume).coerceIn(0f, 1f)
    return withContext(Dispatchers.Default) { drawMusicPoster(song, artwork, positionMs, durationMs, playing, volume, surface) }
}

private fun drawMusicPoster(
    song: SongUiModel,
    artwork: Bitmap?,
    positionMs: Long,
    durationMs: Long,
    playing: Boolean,
    volume: Float,
    surface: MusicShareSurface,
): ImageBitmap {
    val output = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawColor(Color.rgb(35, 37, 42))
    if (artwork != null && artwork.width > 0 && artwork.height > 0) {
        val small = Bitmap.createScaledBitmap(artwork, 18, 18, true)
        canvas.drawBitmap(small, null, Rect(0, 0, 1080, 1920), paint)
        if (small !== artwork) small.recycle()
    }
    paint.shader = LinearGradient(0f, 0f, 1080f, 1920f, 0xAA000000.toInt(), 0xDD17171C.toInt(), Shader.TileMode.CLAMP)
    canvas.drawRect(0f, 0f, 1080f, 1920f, paint)
    paint.shader = null
    val cardRect = RectF(80f, 165f, 1000f, 1740f)
    if (surface != MusicShareSurface.FLAT && artwork != null && artwork.width > 0 && artwork.height > 0) {
        val detail =
            when (surface) {
                MusicShareSurface.CLEAR -> 160
                MusicShareSurface.BLUR -> 24
                MusicShareSurface.FROSTED -> 10
                MusicShareSurface.FLAT -> 18
            }
        val backdrop = Bitmap.createScaledBitmap(artwork, detail, detail, true)
        val clip = Path().apply { addRoundRect(cardRect, 88f, 88f, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clip)
        paint.color = Color.WHITE
        canvas.drawBitmap(backdrop, null, Rect(0, 0, 1080, 1920), paint)
        canvas.restore()
        if (backdrop !== artwork) backdrop.recycle()
    }
    paint.color =
        when (surface) {
            MusicShareSurface.FLAT -> 0xD91B1B20.toInt()
            MusicShareSurface.CLEAR -> 0x991B1B20.toInt()
            MusicShareSurface.BLUR -> 0xB51B1B20.toInt()
            MusicShareSurface.FROSTED -> 0xC95C5C63.toInt()
        }
    canvas.drawRoundRect(cardRect, 88f, 88f, paint)
    if (surface != MusicShareSurface.FLAT) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0x55FFFFFF
        canvas.drawRoundRect(cardRect, 88f, 88f, paint)
        paint.style = Paint.Style.FILL
    }

    val artRect = RectF(150f, 235f, 930f, 1015f)
    paint.color = Color.rgb(42, 42, 48)
    canvas.drawRoundRect(artRect, 18f, 18f, paint)
    if (artwork != null && artwork.width > 0 && artwork.height > 0) {
        val side = min(artwork.width, artwork.height)
        val left = (artwork.width - side) / 2
        val top = (artwork.height - side) / 2
        val clip = Path().apply { addRoundRect(artRect, 18f, 18f, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(clip)
        paint.color = Color.WHITE
        canvas.drawBitmap(artwork, Rect(left, top, left + side, top + side), artRect, paint)
        canvas.restore()
    }

    fun text(
        value: String,
        y: Float,
        size: Float,
        color: Int,
        bold: Boolean = false,
        maxWidth: Float = 780f,
    ) {
        val p =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = size
                typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
            }
        canvas.drawText(TextUtils.ellipsize(value, p, maxWidth, TextUtils.TruncateAt.END).toString(), 150f, y, p)
    }
    text("Metrolist", 1080f, 34f, 0xFFB8B5B8.toInt(), bold = true)
    text(song.title, 1140f, 48f, Color.WHITE, bold = true)
    text(song.artists.joinToString(", ") { it.name }, 1195f, 40f, 0xFFB8B5B8.toInt())
    val safeDuration = durationMs.coerceAtLeast(0L)
    val safePosition = positionMs.coerceAtLeast(0L).let { if (safeDuration > 0) it.coerceAtMost(safeDuration) else it }
    val progress = if (safeDuration > 0) safePosition.toFloat() / safeDuration else 0f

    fun clock(ms: Long): String {
        val seconds = ms / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }
    text(clock(safePosition), 1280f, 30f, 0xFFBBB8BB.toInt())
    val remaining = if (safeDuration > 0) "−${clock(safeDuration - safePosition)}" else "−0:00"
    val timePaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFBBB8BB.toInt()
            textSize = 30f
            textAlign = Paint.Align.RIGHT
        }
    canvas.drawText(remaining, 930f, 1280f, timePaint)
    paint.color = 0xFF777477.toInt()
    canvas.drawRoundRect(RectF(255f, 1258f, 825f, 1274f), 8f, 8f, paint)
    paint.color = 0xFFE8E5E6.toInt()
    canvas.drawRoundRect(RectF(255f, 1258f, 255f + 570f * progress, 1274f), 8f, 8f, paint)

    fun triangle(
        x: Float,
        y: Float,
        right: Boolean,
    ) {
        val direction = if (right) 1f else -1f
        val path =
            Path().apply {
                moveTo(x - 24f * direction, y - 28f)
                lineTo(x + 24f * direction, y)
                lineTo(x - 24f * direction, y + 28f)
                close()
            }
        canvas.drawPath(path, paint)
    }
    paint.color = 0xFFCBC8CA.toInt()
    triangle(355f, 1400f, false)
    triangle(390f, 1400f, false)
    triangle(690f, 1400f, true)
    triangle(725f, 1400f, true)
    paint.color = Color.WHITE
    if (playing) {
        canvas.drawRoundRect(RectF(510f, 1355f, 528f, 1445f), 6f, 6f, paint)
        canvas.drawRoundRect(RectF(552f, 1355f, 570f, 1445f), 6f, 6f, paint)
    } else {
        triangle(540f, 1400f, true)
    }
    paint.color = 0xFF777477.toInt()
    canvas.drawRoundRect(RectF(230f, 1550f, 850f, 1566f), 8f, 8f, paint)
    paint.color = 0xFFE8E5E6.toInt()
    canvas.drawRoundRect(RectF(230f, 1550f, 230f + 620f * volume, 1566f), 8f, 8f, paint)
    return output.asImageBitmap()
}
