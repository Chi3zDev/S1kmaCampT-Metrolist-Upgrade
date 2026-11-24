package com.metrolist.android.ui

import com.metrolist.shared.ui.navigation.glassNavigationMotionAllowed
import com.metrolist.shared.ui.navigation.glassMorphNavWidth
import com.metrolist.shared.ui.navigation.glassMorphNavbarBottomOffset
import com.metrolist.shared.ui.navigation.glassMorphPlayerDown
import com.metrolist.shared.ui.navigation.glassMorphPlayerStart
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class GlassMorphGeometryTest {
    @Test
    fun motionRequiresSystemDeviceVisibilityAndUserPreferences() {
        assertTrue(glassNavigationMotionAllowed(true, true, true, false, true))
        assertFalse(glassNavigationMotionAllowed(false, true, true, false, true))
        assertFalse(glassNavigationMotionAllowed(true, false, true, false, true))
        assertFalse(glassNavigationMotionAllowed(true, true, false, false, true))
        assertFalse(glassNavigationMotionAllowed(true, true, true, true, true))
        assertFalse(glassNavigationMotionAllowed(true, true, true, false, false))
    }

    @Test
    fun expandedAndCompactWidthsShareOneProgress() {
        assertEquals(400f, glassMorphNavWidth(400f, 0f), .001f)
        assertEquals(248f, glassMorphNavWidth(400f, .5f), .001f)
        assertEquals(96f, glassMorphNavWidth(400f, 1f), .001f)
        assertEquals(0f, glassMorphPlayerStart(400f, 0f), .001f)
        assertEquals(48f, glassMorphPlayerStart(400f, .5f), .001f)
        assertEquals(96f, glassMorphPlayerStart(400f, 1f), .001f)
    }

    @Test
    fun bothPlayerPositionsEndOnTheSameRow() {
        assertEquals(80f, glassMorphPlayerDown(104f, 24f, false, 1f), .001f)
        assertEquals(0f, glassMorphPlayerDown(104f, 24f, true, 1f), .001f)
        assertEquals(72f, glassMorphNavbarBottomOffset(64f, 8f, true, 0f), .001f)
        assertEquals(0f, glassMorphNavbarBottomOffset(64f, 8f, true, 1f), .001f)
        assertEquals(0f, glassMorphNavbarBottomOffset(64f, 8f, false, .5f), .001f)
    }

    @Test
    fun clampsSpringOvershootAndSmallWindows() {
        assertEquals(96f, glassMorphNavWidth(400f, 2f), .001f)
        assertEquals(400f, glassMorphNavWidth(400f, -1f), .001f)
        assertEquals(80f, glassMorphNavWidth(80f, 1f), .001f)
        assertEquals(80f, glassMorphPlayerStart(80f, 2f), .001f)
        assertEquals(0f, glassMorphPlayerDown(10f, 24f, false, 1f), .001f)
    }
}
