package com.metrolist.shared.ui.components.lyrics

import androidx.compose.ui.text.style.TextAlign
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
