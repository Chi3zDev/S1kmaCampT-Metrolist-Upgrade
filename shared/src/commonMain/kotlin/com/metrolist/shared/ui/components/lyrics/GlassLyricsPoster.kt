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

internal data class GlassPosterFooterLayout(val artworkX: Float, val labelX: Float, val labelWidth: Float)

/** Align the artwork to the reading-direction start; reclaim its space when unavailable. */
internal fun glassPosterFooterLayout(
    left: Float, width: Float, inset: Float, artSize: Float, gap: Float,
    hasArtwork: Boolean, direction: LayoutDirection,
): GlassPosterFooterLayout {
    val start = left + inset
    val end = left + width - inset
    val used = if (hasArtwork) artSize + gap else 0f
    return if (direction == LayoutDirection.Rtl) {
        GlassPosterFooterLayout(end - artSize, start, (end - start - used).coerceAtLeast(1f))
    } else {
        GlassPosterFooterLayout(start, start + used, (end - start - used).coerceAtLeast(1f))
    }
}

internal data class GlassPosterCoverCrop(val offset: IntOffset, val size: IntSize)

/** Center-crop to the target aspect ratio without stretching rectangular artwork. */
internal fun glassPosterCoverCrop(source: IntSize, target: IntSize): GlassPosterCoverCrop {
    require(source.width > 0 && source.height > 0 && target.width > 0 && target.height > 0)
    val sourceIsWider = source.width.toLong() * target.height > target.width.toLong() * source.height
    val cropWidth = if (sourceIsWider) (source.height.toLong() * target.width / target.height).toInt().coerceAtLeast(1) else source.width
    val cropHeight = if (sourceIsWider) source.height else (source.width.toLong() * target.height / target.width).toInt().coerceAtLeast(1)
    return GlassPosterCoverCrop(IntOffset((source.width - cropWidth) / 2, (source.height - cropHeight) / 2), IntSize(cropWidth, cropHeight))
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
                val target = IntSize(bitmap.width, bitmap.height)
                val crop = glassPosterCoverCrop(IntSize(image.width, image.height), target)
                drawImage(image, srcOffset = crop.offset,
                    srcSize = crop.size, dstSize = target, filterQuality = FilterQuality.High)
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
            val footer = glassPosterFooterLayout(cardLeft, cardWidth, inner, artSize, 12f * scale, artwork != null, layoutDirection)
            val artX = footer.artworkX
            val artY = footerTop + (footerHeight - artSize) / 2f
            artwork?.let { image ->
                val crop = minOf(image.width, image.height)
                val artPath = Path().apply { addRoundRect(RoundRect(artX, artY, artX + artSize, artY + artSize, CornerRadius(6f * scale))) }
                clipPath(artPath) {
                    drawImage(image, srcOffset = IntOffset((image.width - crop) / 2, (image.height - crop) / 2), srcSize = IntSize(crop, crop),
                        dstOffset = IntOffset(artX.toInt(), artY.toInt()), dstSize = IntSize(artSize.toInt(), artSize.toInt()), filterQuality = FilterQuality.High)
                }
            }
            val labelX = footer.labelX
            val labelWidth = footer.labelWidth.toInt().coerceAtLeast(1)
            fun label(value: String, size: Float, y: Float, color: Color, weight: FontWeight) {
                fun measure(fontSize: Float) = textMeasurer.measure(value, style = TextStyle(color = color, fontSize = fontSize.sp, fontWeight = weight, textAlign = androidx.compose.ui.text.style.TextAlign.Start),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = Constraints(minWidth = labelWidth, maxWidth = labelWidth), density = density, layoutDirection = layoutDirection)
                val fittedSize = fitGlassPosterLabelFontSize(size, minimum = 8f) { fontSize ->
                    val result = measure(fontSize)
                    result.didOverflowWidth || (result.lineCount > 0 && result.isLineEllipsized(0))
                }
                drawText(measure(fittedSize), topLeft = Offset(labelX, y))
            }
            label(title, 14f, artY, style.textColor, FontWeight.Bold)
            label(artist, 12f, artY + 19f * scale, style.secondaryColor, FontWeight.Normal)
            val logoSize = 10f * scale
            val appGap = 13f * scale
            val rtl = layoutDirection == LayoutDirection.Rtl
            val logoX = if (rtl) labelX + labelWidth - logoSize else labelX
            translate(logoX, artY + 36f * scale) {
                with(logo) { draw(Size(logoSize, logoSize), colorFilter = ColorFilter.tint(style.secondaryColor)) }
            }
            val appWidth = (labelWidth - appGap).toInt().coerceAtLeast(1)
            val appLayout = textMeasurer.measure(appName, style = TextStyle(color = style.secondaryColor, fontSize = 9f.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Start),
                maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = Constraints(minWidth = appWidth, maxWidth = appWidth), density = density, layoutDirection = layoutDirection)
            drawText(appLayout, topLeft = Offset(if (rtl) labelX else labelX + appGap, artY + 35f * scale))
        }
        drawRoundRect(Color.White.copy(alpha = 0.2f), topLeft = Offset(cardLeft, cardTop), size = Size(cardWidth, cardHeight), cornerRadius = CornerRadius(radius), style = Stroke(scale))
    }
    return bitmap
}
