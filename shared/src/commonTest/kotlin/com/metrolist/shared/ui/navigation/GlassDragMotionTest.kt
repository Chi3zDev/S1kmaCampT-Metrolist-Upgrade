package com.metrolist.shared.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlassDragMotionTest {
    @Test
    fun sameSpeedProducesSameStretchAtDifferentFrameRates() {
        assertEquals(glassDragStretch(8f, 16, 1f), glassDragStretch(4f, 8, 1f), 0.0001f)
        assertEquals(glassDragStretch(16f, 16, 2f), glassDragStretch(8f, 16, 1f), 0.0001f)
    }

    @Test
    fun stretchIsBoundedForFastAndSlowGestures() {
        assertEquals(1f, glassDragStretch(0f, 16, 1f), 0.0001f)
        assertTrue(glassDragStretch(4f, 16, 1f) < glassDragStretch(40f, 16, 1f))
        assertEquals(1.18f, glassDragStretch(10000f, 0, 1f), 0.0001f)
    }
}
