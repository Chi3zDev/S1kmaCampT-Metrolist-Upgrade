package com.metrolist.shared.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlassThemeTest {
    @Test
    fun lightThemeUsesWhiteHighlightNotDarkInk() {
        val (highlight, edge) = glassEdgeColors(Color.White, Color.Black, Color.DarkGray, clear = true)
        assertEquals(Color.White.copy(alpha = 0.72f), highlight)
        assertTrue(edge.alpha <= 0.17f)
    }

    @Test
    fun darkThemeRetainsSubtleHighlight() {
        val (highlight, _) = glassEdgeColors(Color.Black, Color.White, Color.LightGray, clear = false)
        assertEquals(Color.White.copy(alpha = 0.25f), highlight)
    }
}
