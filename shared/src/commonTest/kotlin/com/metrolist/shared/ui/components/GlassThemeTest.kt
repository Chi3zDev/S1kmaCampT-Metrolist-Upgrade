package com.metrolist.shared.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlassThemeTest {
    @Test
    fun dialogTintMaintainsContrastWithoutDiscardingTheBackdrop() {
        assertEquals(Color.White.copy(alpha = 0.86f), glassDialogTint(Color.White.copy(alpha = 0.3f)))
        assertEquals(Color.Black.copy(alpha = 0.9f), glassDialogTint(Color.Black.copy(alpha = 0.9f)))
    }

    @Test
    fun pageFallsBackToThemeWhenImageIsUnavailable() {
        assertEquals(Color.Blue, pageBackgroundColor(Color.Blue, imageReady = false))
        assertEquals(Color.Transparent, pageBackgroundColor(Color.Blue, imageReady = true))
    }

    @Test
    fun toolbarRemainsOpaqueWithoutAWorkingBackdrop() {
        assertEquals(Color.Blue, topBarBackgroundColor(Color.Blue, imageReady = true, glassReady = false))
        assertEquals(Color.Blue, topBarBackgroundColor(Color.Blue, imageReady = false, glassReady = true))
        assertEquals(Color.Blue.copy(alpha = 0.82f), topBarBackgroundColor(Color.Blue, imageReady = true, glassReady = true))
    }

    @Test
    fun themeAndFloatingCardsAreIndependent() {
        assertTrue(glassListMaterialEnabled(liquidGlassTheme = true, floatingGlass = false, glassCards = false))
        assertTrue(glassListMaterialEnabled(liquidGlassTheme = false, floatingGlass = true, glassCards = true))
        assertTrue(!glassListMaterialEnabled(liquidGlassTheme = false, floatingGlass = true, glassCards = false))
        assertTrue(!glassListMaterialEnabled(liquidGlassTheme = false, floatingGlass = false, glassCards = true))
    }

    @Test
    fun backdropMustExistBeforeUsingTransparentGlass() {
        assertTrue(glassBackdropReady(themeEnabled = true, backdropPresent = true))
        assertTrue(!glassBackdropReady(themeEnabled = true, backdropPresent = false))
        assertTrue(!glassBackdropReady(themeEnabled = false, backdropPresent = true))
        assertTrue(!glassBackdropReady(themeEnabled = false, backdropPresent = false))
    }

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
