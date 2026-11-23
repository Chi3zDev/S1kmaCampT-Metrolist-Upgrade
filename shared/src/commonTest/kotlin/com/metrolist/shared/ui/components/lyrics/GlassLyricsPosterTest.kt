package com.metrolist.shared.ui.components.lyrics

import androidx.compose.ui.text.style.TextAlign
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlassLyricsPosterTest {
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
}
