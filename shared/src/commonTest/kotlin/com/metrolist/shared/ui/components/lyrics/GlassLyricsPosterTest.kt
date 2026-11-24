package com.metrolist.shared.ui.components.lyrics

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlassLyricsPosterTest {
    @Test
    fun cardWidthUsesWholeSliderRangeAndKeepsSafeDefaults() {
        assertEquals(0.78f, glassPosterCardWidthFraction(1f), 0.0001f)
        assertEquals(0.468f, glassPosterCardWidthFraction(0.6f), 0.0001f)
        assertEquals(0.90f, glassPosterCardWidthFraction(1.35f), 0.0001f)
        assertTrue(glassPosterCardWidthFraction(0.6f) < glassPosterCardWidthFraction(0.7f))
        assertTrue(glassPosterCardWidthFraction(1.15f) < glassPosterCardWidthFraction(1.35f))
        assertEquals(glassPosterCardWidthFraction(0.6f), glassPosterCardWidthFraction(-1f))
        assertEquals(glassPosterCardWidthFraction(1.35f), glassPosterCardWidthFraction(2f))
        assertEquals(0.78f, glassPosterCardWidthFraction(Float.NaN), 0.0001f)
        assertEquals(0.78f, glassPosterCardWidthFraction(Float.POSITIVE_INFINITY), 0.0001f)
    }

    @Test
    fun styleRoundTripsWithoutLosingFontOrBackground() {
        val style = LyricsImageStyle(
            posterStyle = LyricsPosterStyle.GLASS_CARD,
            background = LyricsImageBackground.BLUR,
            font = LyricsPosterFont.NUNITO_EXTRABOLD,
            textScale = 1.2f,
        )
        val restored = decodeLyricsImageStyle(style.encodeForStorage(), TextAlign.Left)
        assertEquals(LyricsPosterStyle.GLASS_CARD, restored.posterStyle)
        assertEquals(LyricsImageBackground.BLUR, restored.background)
        assertEquals(LyricsPosterFont.NUNITO_EXTRABOLD, restored.font)
        assertEquals(1.2f, restored.textScale)
    }

    @Test
    fun previousStyleDefaultsToClassic() {
        val legacy = """{"background":"SOLID","backgroundColor":0,"textColor":0,"secondaryColor":0,"textScale":1.0,"contentScale":1.0,"font":"SANS"}"""
        assertEquals(LyricsPosterStyle.CLASSIC, decodeLyricsImageStyle(legacy, TextAlign.Center).posterStyle)
    }
    @Test
    fun veryTallTextUsesDrawScaleToStayInsideAvailableHeight() {
        val fit = fitLyricsImageText(height = 20f, textScale = 1f) { 100f }
        assertTrue(fit.drawScale < 1f)
        assertTrue(100f * fit.drawScale <= 20f)
    }

    @Test
    fun fittingTextKeepsFullDrawScale() {
        val fit = fitLyricsImageText(height = 50f, textScale = 1f) { it * 2f }
        assertEquals(1f, fit.drawScale)
        assertTrue(fit.fontSize * 2f <= 50f)
    }

    @Test
    fun unknownPosterFontAndStyleFallBackSafely() {
        val stored = """{"background":"SOLID","backgroundColor":0,"textColor":0,"secondaryColor":0,"textScale":1.0,"contentScale":1.0,"font":"REMOVED_FONT","posterStyle":"REMOVED_STYLE"}"""
        val restored = decodeLyricsImageStyle(stored, TextAlign.Center)
        assertEquals(LyricsPosterStyle.CLASSIC, restored.posterStyle)
        assertEquals(LyricsPosterFont.SANS, restored.font)
    }
    @Test
    fun glassPosterKeepsLightTextOnItsDarkCard() {
        assertEquals(Color.White, lyricsPosterTextColor(LyricsPosterStyle.GLASS_CARD, true, 0.95f))
        assertEquals(Color.Black, lyricsPosterTextColor(LyricsPosterStyle.CLASSIC, true, 0.95f))
        assertEquals(Color.White, lyricsPosterTextColor(LyricsPosterStyle.CLASSIC, true, 0.1f))
    }
    @Test
    fun scaledPosterTextRespectsStartEndAndLayoutDirection() {
        assertEquals(0f, lyricsPosterScaleOffset(TextAlign.Start, LayoutDirection.Ltr, 100f, 0.5f))
        assertEquals(50f, lyricsPosterScaleOffset(TextAlign.Start, LayoutDirection.Rtl, 100f, 0.5f))
        assertEquals(0f, lyricsPosterScaleOffset(TextAlign.End, LayoutDirection.Rtl, 100f, 0.5f))
        assertEquals(25f, lyricsPosterScaleOffset(TextAlign.Center, LayoutDirection.Ltr, 100f, 0.5f))
    }
}
