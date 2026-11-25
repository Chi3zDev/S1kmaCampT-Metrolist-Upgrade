package com.metrolist.shared.ui.player

import com.metrolist.shared.settings.PlayerThumbnailStyle
import com.metrolist.shared.settings.PlayerType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerThumbnailPolicyTest {
    @Test
    fun canvasDoesNotOverrideOtherThumbnailModes() {
        for (type in PlayerType.entries) {
            assertFalse(usesCanvasBackdrop(PlayerThumbnailStyle.COMPACT, type))
            assertFalse(usesCanvasBackdrop(PlayerThumbnailStyle.FULLSCREEN, type))
            assertTrue(usesCanvasBackdrop(PlayerThumbnailStyle.CANVAS, type) == (type != PlayerType.VINYL))
        }
    }
}
