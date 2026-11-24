package com.metrolist.shared.ui.components.lyrics

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.metrolist.shared.ui.theme.getGradientColors

/** Use the whole editor range while retaining the original default width and safe side margins. */
internal fun glassPosterCardWidthFraction(contentScale: Float): Float {
    val scale = contentScale.takeIf { it.isFinite() }?.coerceIn(0.6f, 1.35f) ?: 1f
    return if (scale <= 1f) 0.78f * scale else 0.78f + (scale - 1f) * (0.12f / 0.35f)
}

/** Shrink long footer labels before falling back to ellipsis at the minimum readable size. */
internal fun fitGlassPosterLabelFontSize(preferred: Float, minimum: Float, overflows: (Float) -> Boolean): Float {
    if (!overflows(preferred)) return preferred
    if (overflows(minimum)) return minimum
    var low = minimum
    var high = preferred
    repeat(8) {
        val middle = (low + high) / 2f
        if (overflows(middle)) high = middle else low = middle
    }
    return low
}

/** The same renderer is used for preview and export. No backdrop blur is applied. */
internal fun renderGlassLyricsPoster(
    lyrics: String,
    title: String,
    artist: String,
    appName: String,
    artwork: ImageBitmap?,
    blurredArtwork: ImageBitmap?,
    palette: List<Color>,
    style: LyricsImageStyle,
    textMeasurer: TextMeasurer,
    logo: Painter,
    layoutDirection: LayoutDirection,
    sizePx: Int,
    story: Boolean,
): ImageBitmap {
    val bitmap = ImageBitmap(sizePx, if (story) sizePx * 16 / 9 else sizePx)
    val scale = sizePx / 340f
    val density = Density(scale, fontScale = 1f)
    val width = bitmap.width.toFloat()
    val height = bitmap.height.toFloat()
    val cardWidth = width * glassPosterCardWidthFraction(style.contentScale)
    val inner = 20f * scale
    val textWidth = cardWidth - 2 * inner
    val constrainedWidth = textWidth.toInt().coerceAtLeast(1)
    val textConstraints = Constraints(minWidth = constrainedWidth, maxWidth = constrainedWidth)
    val maxTextHeight = (height * 0.55f - 78f * scale).coerceAtLeast(50f * scale)
    val text = lyrics.lines().take(6).joinToString("\n")
    val family = style.fontFamilyOverride ?: style.font.family

    fun measureText(fontSize: Float) = textMeasurer.measure(
        text,
        style = TextStyle(color = style.textColor, fontFamily = family, fontWeight = if (style.font == LyricsPosterFont.NUNITO_EXTRABOLD) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = fontSize.sp, lineHeight = (fontSize * 1.15f).sp, textAlign = style.alignment),
        constraints = textConstraints,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Clip,
        density = density,
        layoutDirection = layoutDirection,
    )
    val fit = fitLyricsImageText(maxTextHeight, style.textScale, maxFontSize = 32f) { measureText(it).size.height.toFloat() }
    val measured = measureText(fit.fontSize)
    val renderedHeight = measured.size.height * fit.drawScale
    val footerHeight = 82f * scale
    val cardHeight = (inner + renderedHeight + inner + footerHeight).coerceAtMost(height * 0.9f)
    val cardLeft = (width - cardWidth) / 2f
    val cardTop = (height - cardHeight) / 2f
    val footerTop = cardTop + cardHeight - footerHeight
    val radius = 20f * scale

    CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(width, height)) {
        drawRect(style.backgroundColor)
        when (style.background) {
            LyricsImageBackground.BLUR, LyricsImageBackground.GLASS -> blurredArtwork?.let { image ->
                val cropWidth = (image.height * width / height).toInt().coerceIn(1, image.width)
                val cropHeight = (image.width * height / width).toInt().coerceIn(1, image.height)
                val source = if (width < height) IntSize(cropWidth, image.height) else IntSize(image.width, cropHeight)
                drawImage(image, srcOffset = IntOffset((image.width - source.width) / 2, (image.height - source.height) / 2),
                    srcSize = source, dstSize = IntSize(width.toInt(), height.toInt()), filterQuality = FilterQuality.High)
            }
            LyricsImageBackground.GRADIENT -> drawRect(Brush.linearGradient(palette.firstOrNull()?.let(::getGradientColors) ?: listOf(style.backgroundColor, style.backgroundColor)))
            LyricsImageBackground.SOLID -> Unit
        }
        drawRect(Color(0xFF180C08).copy(alpha = 0.52f))
        val cardPath = Path().apply { addRoundRect(RoundRect(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight, CornerRadius(radius))) }
        clipPath(cardPath) {
            drawRect(Color(0xFF191412).copy(alpha = 0.67f), topLeft = Offset(cardLeft, cardTop), size = Size(cardWidth, cardHeight))
            drawRect(Color.Black.copy(alpha = 0.23f), topLeft = Offset(cardLeft, footerTop), size = Size(cardWidth, footerHeight))
            drawLine(Color.White.copy(alpha = 0.14f), Offset(cardLeft, footerTop), Offset(cardLeft + cardWidth, footerTop), strokeWidth = scale * 0.7f)
            val alignmentOffset = lyricsPosterScaleOffset(style.alignment, layoutDirection, textWidth, fit.drawScale)
            translate(cardLeft + inner + alignmentOffset, cardTop + inner) {
                scale(fit.drawScale, pivot = Offset.Zero) { drawText(measured) }
            }
            val artSize = 50f * scale
            val artX = cardLeft + inner
            val artY = footerTop + (footerHeight - artSize) / 2f
            artwork?.let { image ->
                val crop = minOf(image.width, image.height)
                val artPath = Path().apply { addRoundRect(RoundRect(artX, artY, artX + artSize, artY + artSize, CornerRadius(6f * scale))) }
                clipPath(artPath) {
                    drawImage(image, srcOffset = IntOffset((image.width - crop) / 2, (image.height - crop) / 2), srcSize = IntSize(crop, crop),
                        dstOffset = IntOffset(artX.toInt(), artY.toInt()), dstSize = IntSize(artSize.toInt(), artSize.toInt()), filterQuality = FilterQuality.High)
                }
            }
            val labelX = artX + artSize + 12f * scale
            val labelWidth = (cardLeft + cardWidth - inner - labelX).toInt().coerceAtLeast(1)
            fun label(value: String, size: Float, y: Float, color: Color, weight: FontWeight) {
                fun measure(fontSize: Float) = textMeasurer.measure(value, style = TextStyle(color = color, fontSize = fontSize.sp, fontWeight = weight),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = labelWidth), density = density, layoutDirection = layoutDirection)
                val fittedSize = fitGlassPosterLabelFontSize(size, minimum = 8f) { fontSize ->
                    val result = measure(fontSize)
                    result.didOverflowWidth || (result.lineCount > 0 && result.isLineEllipsized(0))
                }
                drawText(measure(fittedSize), topLeft = Offset(labelX, y))
            }
            label(title, 14f, artY, style.textColor, FontWeight.Bold)
            label(artist, 12f, artY + 19f * scale, style.secondaryColor, FontWeight.Normal)
            translate(labelX, artY + 36f * scale) {
                with(logo) { draw(Size(10f * scale, 10f * scale), colorFilter = ColorFilter.tint(style.secondaryColor)) }
            }
            val appLayout = textMeasurer.measure(appName, style = TextStyle(color = style.secondaryColor, fontSize = 9f.sp),
                maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = (labelWidth - 12f * scale).toInt().coerceAtLeast(1)), density = density, layoutDirection = layoutDirection)
            drawText(appLayout, topLeft = Offset(labelX + 13f * scale, artY + 35f * scale))
        }
        drawRoundRect(Color.White.copy(alpha = 0.2f), topLeft = Offset(cardLeft, cardTop), size = Size(cardWidth, cardHeight), cornerRadius = CornerRadius(radius), style = Stroke(scale))
    }
    return bitmap
}
