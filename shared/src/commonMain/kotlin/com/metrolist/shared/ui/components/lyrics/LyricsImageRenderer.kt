/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.metrolist.shared.ui.theme.getGradientColors

internal enum class LyricsImageBackground { GLASS, SOLID, BLUR, GRADIENT }

internal enum class LyricsPosterFont(val family: FontFamily) {
    SANS(FontFamily.SansSerif), SERIF(FontFamily.Serif), MONO(FontFamily.Monospace), HANDWRITING(FontFamily.Cursive),
    NUNITO_BOLD(FontFamily.SansSerif), NUNITO_EXTRABOLD(FontFamily.SansSerif),
}

internal data class LyricsImageStyle(
    val background: LyricsImageBackground = LyricsImageBackground.SOLID,
    val backgroundColor: Color = Color(0xFF242424),
    val textColor: Color = Color.White,
    val secondaryColor: Color = Color.White.copy(alpha = 0.7f),
    val alignment: TextAlign = TextAlign.Center,
    val textScale: Float = 1f,
    val contentScale: Float = 1f,
    val font: LyricsPosterFont = LyricsPosterFont.SANS,
    val fontFamilyOverride: FontFamily? = null,
    val glassEnabled: Boolean = false,
    val glassStyle: String = "BLUR",
    val glassTint: Float = 0.55f,
)

private fun DrawScope.drawCover(
    image: ImageBitmap,
    topLeft: Offset,
    side: Float,
) {
    val crop = minOf(image.width, image.height)
    drawImage(
        image,
        srcOffset = IntOffset((image.width - crop) / 2, (image.height - crop) / 2),
        srcSize = IntSize(crop, crop),
        dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
        dstSize = IntSize(side.toInt(), side.toInt()),
        filterQuality = FilterQuality.High,
    )
}

internal fun prepareLyricsArtwork(image: ImageBitmap, blurRadius: Int = 6): Pair<List<Color>, ImageBitmap> {
    val sample = ImageBitmap(64, 64)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(sample), Size(64f, 64f)) {
        drawRect(Color.Black)
        drawCover(image, Offset.Zero, 64f)
    }
    val pixels = IntArray(64 * 64)
    sample.readPixels(pixels)
    val palette = lyricsArtworkPalette(pixels)
    val blurred = blurLyricsArtwork(pixels, 64, 64, blurRadius.coerceIn(0, 9))
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(sample), Size(64f, 64f)) {
        blurred.forEachIndexed { index, pixel ->
            drawRect(Color(pixel), Offset((index % 64).toFloat(), (index / 64).toFloat()), Size(1f, 1f))
        }
    }
    return palette to sample
}

internal fun renderLyricsImage(
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
    sizePx: Int = 2160,
    story: Boolean = false,
    storyBackgroundColor: Color = style.backgroundColor,
    storyCardColor: Color = style.backgroundColor,
    storyTextColor: Color = style.textColor,
    storySecondaryColor: Color = style.secondaryColor,
    shareUrl: String? = null,
): ImageBitmap {
    val bitmap = ImageBitmap(sizePx, if (story) sizePx * 16 / 9 else sizePx)
    val scale = sizePx / 340f
    val density = Density(scale, fontScale = 1f)
    val layout = lyricsImageLayout(style.contentScale, story)
    val contentScale = layout.contentScale
    val padding = layout.padding * scale
    val width = layout.lyricsWidth * scale
    val lyricsTop = layout.lyricsTop * scale
    val lyricsHeight = layout.lyricsHeight * scale
    val lyricsInsetX = if (story) 18f * contentScale * scale else 0f
    val lyricsInsetY = if (story) 12f * contentScale * scale else 0f
    val textWidth = width - 2 * lyricsInsetX
    val textHeight = lyricsHeight - 2 * lyricsInsetY
    val textLeft = padding + lyricsInsetX
    val textStyle =
        TextStyle(
            color = style.textColor,
            fontWeight = FontWeight.Bold,
            fontFamily = style.fontFamilyOverride ?: style.font.family,
            textAlign = style.alignment,
        )

    fun lyricsText(size: Float) =
        buildAnnotatedString {
            lyrics.lines().forEachIndexed { index, line ->
                if (index > 0) {
                    withStyle(ParagraphStyle(lineHeight = (size * 0.5f).sp)) {
                        withStyle(SpanStyle(fontSize = (size * 0.5f).sp)) { append('\u200B') }
                    }
                }
                withStyle(ParagraphStyle(lineHeight = (size * 1.2f).sp)) { append(line) }
            }
        }

    fun measureLyrics(size: Float) =
        textMeasurer.measure(
            lyricsText(size),
            style = textStyle.copy(fontSize = size.sp, lineHeight = (size * 1.2f).sp),
            constraints = Constraints(minWidth = textWidth.toInt(), maxWidth = textWidth.toInt()),
            density = density,
            layoutDirection = layoutDirection,
        )
    val fit =
        fitLyricsImageText(textHeight, style.textScale, maxFontSize = if (story) 34f else 50f) {
            measureLyrics(it).size.height.toFloat()
        }
    val lyricsLayout = measureLyrics(fit.fontSize)
    val renderedLyricsHeight = lyricsLayout.size.height * fit.drawScale
    val footerTop =
        if (story) {
            minOf(layout.footerTop, layout.lyricsTop + 12f * contentScale + renderedLyricsHeight / scale + 18f * contentScale)
        } else {
            layout.footerTop
        }
    val cardBottom = if (story) footerTop + 34f * contentScale else 0f
    val cardHeight = if (story) cardBottom - layout.cardTop else 0f
    val verticalOffset = if (story) ((340f * 16f / 9f - cardHeight) / 2f) - layout.cardTop else 0f
    val verticalOffsetPx = verticalOffset * scale
    val textTop =
        lyricsTop + lyricsInsetY + verticalOffsetPx +
            if (story) 0f else (textHeight - renderedLyricsHeight) / 2f
    val textClipHeight = if (story) renderedLyricsHeight else textHeight
    CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), Size(bitmap.width.toFloat(), bitmap.height.toFloat())) {
        val path =
            Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(if (story) 0f else 20 * scale)))
            }
        clipPath(path) {
            drawRect(style.backgroundColor)
            when (style.background) {
                LyricsImageBackground.SOLID -> Unit
                LyricsImageBackground.BLUR, LyricsImageBackground.GLASS -> {
                    blurredArtwork?.let {
                        drawImage(it, dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
                        if (style.background == LyricsImageBackground.GLASS && style.glassEnabled) {
                            val styleTint = when (style.glassStyle) { "FROSTED" -> 0.65f; "CLEAR" -> 0.22f; else -> 0.42f }
                            drawRect(style.backgroundColor.copy(alpha = (styleTint * style.glassTint).coerceIn(0f, 0.65f)))
                            drawRect(Color.Black.copy(alpha = if (style.glassStyle == "CLEAR") 0.48f else 0.55f))
                            val inset = 4f * scale
                            val edgeSize = Size(size.width - 2 * inset, size.height - 2 * inset)
                            val corner = CornerRadius(16f * scale)
                            drawRoundRect(Color.Black.copy(alpha = 0.18f), topLeft = Offset(inset, inset + 2f * scale), size = edgeSize, cornerRadius = corner, style = Stroke(3f * scale))
                            drawRoundRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = if (style.glassStyle == "CLEAR") 0.65f else 0.35f), Color.White.copy(alpha = 0.06f))), topLeft = Offset(inset, inset), size = edgeSize, cornerRadius = corner, style = Stroke(scale))
                        } else {
                            drawRect(Color.Black.copy(alpha = if (style.background == LyricsImageBackground.GLASS) 0.65f else 0.55f))
                        }
                    }
                }
                LyricsImageBackground.GRADIENT -> {
                    val colors = palette.firstOrNull()?.let(::getGradientColors) ?: listOf(style.backgroundColor, style.backgroundColor)
                    drawRect(Brush.linearGradient(colors))
                    drawRect(Color.Black.copy(alpha = 0.55f))
                }
            }
            artwork?.let {
                val topLeft = Offset(layout.artworkX * scale, (layout.artworkTop + verticalOffset) * scale)
                val artworkSide = layout.artworkSize * scale
                if (story) {
                    val artworkClip =
                        Path().apply {
                            addRoundRect(
                                RoundRect(
                                    topLeft.x,
                                    topLeft.y,
                                    topLeft.x + artworkSide,
                                    topLeft.y + artworkSide,
                                    CornerRadius(18 * scale),
                                ),
                            )
                        }
                    clipPath(artworkClip) { drawCover(it, topLeft, artworkSide) }
                } else {
                    drawCover(it, topLeft, artworkSide)
                }
            }

            fun label(
                text: String,
                x: Float,
                y: Float,
                fontSize: Float,
                color: Color,
                maxWidth: Float,
                shrinkToFit: Boolean = false,
            ) {
                fun measure(fontSize: Float) =
                    textMeasurer.measure(
                        text,
                        style =
                            TextStyle(
                                color = color,
                                fontSize = fontSize.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        constraints =
                            Constraints(
                                minWidth = 0,
                                maxWidth = (maxWidth * scale).toInt(),
                            ),
                        density = density,
                        layoutDirection = layoutDirection,
                    )
                var resolvedFontSize = fontSize
                var textLayout = measure(resolvedFontSize)
                while (shrinkToFit && (textLayout.didOverflowWidth || (textLayout.lineCount > 0 && textLayout.isLineEllipsized(0))) &&
                    resolvedFontSize > 4f
                ) {
                    resolvedFontSize = (resolvedFontSize - 0.25f).coerceAtLeast(4f)
                    textLayout = measure(resolvedFontSize)
                }
                drawText(textLayout, topLeft = Offset(x * scale, y * scale))
            }
            val headerX =
                if (story && artwork != null) {
                    layout.artworkX + layout.artworkSize + 14f * contentScale
                } else if (story) {
                    layout.padding + 18f * contentScale
                } else if (artwork == null) {
                    layout.padding
                } else {
                    layout.padding + 80f * contentScale
                }
            val headerWidth =
                if (story && artwork != null) {
                    layout.lyricsWidth - (headerX - layout.padding) - 18f * contentScale
                } else if (story) {
                    layout.lyricsWidth - 36f * contentScale
                } else {
                    340f - layout.padding - headerX
                }
            label(
                title,
                headerX,
                layout.titleTop + verticalOffset,
                20f * contentScale,
                style.textColor,
                headerWidth,
            )
            label(
                artist,
                headerX,
                layout.artistTop + verticalOffset,
                16f * contentScale,
                style.secondaryColor,
                headerWidth,
            )
            val textX =
                textLeft +
                    when (style.alignment) {
                        TextAlign.Left -> 0f
                        TextAlign.Right -> textWidth * (1f - fit.drawScale)
                        else -> textWidth * (1f - fit.drawScale) / 2f
                    }
            clipRect(textLeft, textTop, textLeft + textWidth, textTop + textClipHeight) {
                translate(textX, textTop) {
                    scale(fit.drawScale, pivot = Offset.Zero) { drawText(lyricsLayout) }
                }
            }
            val footerScale = scale * contentScale
            drawCircle(
                style.secondaryColor,
                radius = (if (story) 10 else 22) * footerScale,
                center =
                    Offset(
                        padding + (if (story) 28f else 22f) * footerScale,
                        (footerTop + verticalOffset) * scale + (if (story) 14 else 22) * footerScale,
                    ),
            )
            translate(
                padding + (if (story) 18f else 3f) * footerScale,
                (footerTop + verticalOffset) * scale + (if (story) 4 else 3) * footerScale,
            ) {
                with(logo) {
                    draw(
                        Size((if (story) 20 else 38) * footerScale, (if (story) 20 else 38) * footerScale),
                        colorFilter = ColorFilter.tint(style.backgroundColor),
                    )
                }
            }
            label(
                appName,
                layout.padding + (if (story) 44f else 52f) * contentScale,
                footerTop + verticalOffset + (if (story) 8f else 13f) * contentScale,
                (if (story) 12f else 14f) * contentScale,
                style.secondaryColor,
                layout.lyricsWidth - (if (story) 62f else 52f) * contentScale,
            )
            if (story && shareUrl != null) {
                label(
                    shareUrl,
                    layout.padding + 44f * contentScale,
                    footerTop + verticalOffset + 20f * contentScale,
                    7f * contentScale,
                    style.secondaryColor,
                    layout.lyricsWidth - 62f * contentScale,
                    shrinkToFit = true,
                )
            }
        }
    }
    return bitmap
}
