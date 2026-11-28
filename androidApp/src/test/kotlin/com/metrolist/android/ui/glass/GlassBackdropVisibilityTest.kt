package com.metrolist.android.ui.glass

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassBackdropVisibilityTest {
    @Test
    fun liveSourceRemainsDrawableWhenPlayerCoversPage() {
        assertFalse(hideBackdropHost(inPictureInPicture = false, contentObscured = true, hasLiveGlassSource = true))
        assertTrue(hideBackdropHost(inPictureInPicture = false, contentObscured = true, hasLiveGlassSource = false))
        assertTrue(hideBackdropHost(inPictureInPicture = true, contentObscured = true, hasLiveGlassSource = true))
        assertFalse(hideBackdropHost(inPictureInPicture = false, contentObscured = false, hasLiveGlassSource = false))
    }

    @Test
    fun liveSourceDoesNotChangeCompositingModeDuringSheetMovement() {
        assertFalse(cacheSheetContentOffscreen(playerProgress = 0.5f, settledExpanded = false, hasLiveGlassSource = true))
        assertTrue(cacheSheetContentOffscreen(playerProgress = 0.5f, settledExpanded = false, hasLiveGlassSource = false))
        assertFalse(cacheSheetContentOffscreen(playerProgress = 0f, settledExpanded = false, hasLiveGlassSource = false))
        assertFalse(cacheSheetContentOffscreen(playerProgress = 1f, settledExpanded = true, hasLiveGlassSource = false))
    }
}
