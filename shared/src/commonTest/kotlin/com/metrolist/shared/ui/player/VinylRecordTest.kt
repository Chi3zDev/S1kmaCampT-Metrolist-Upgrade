package com.metrolist.shared.ui.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VinylRecordTest {
    @Test
    fun rotationAndRestPositionAreStable() {
        assertTrue(360f / VINYL_PLAYING_DEGREES_PER_SECOND in 5f..6f)
        assertEquals(vinylArmContact(0.5f), vinylArmTarget(0.5f, true))
        assertEquals(vinylArmTarget(0f, false), vinylArmTarget(1f, false))
        assertTrue(vinylArmTarget(0f, false).x > vinylArmContact(0f).x)
    }

    @Test
    fun armMovesInwardAndClampsInvalidProgress() {
        val start = vinylArmContact(0f)
        val end = vinylArmContact(1f)
        assertTrue(end.x < start.x)
        assertTrue(end.y < start.y)
        assertEquals(start, vinylArmContact(-1f))
        assertEquals(end, vinylArmContact(2f))
        assertEquals(start, vinylArmContact(Float.NaN))
        // With the record centred at (0.35, 0.5), the stylus stays outside the 58% label.
        val dx = end.x - 0.35f
        val dy = end.y - 0.5f
        assertTrue(dx * dx + dy * dy > (0.58f * 1.05f / 2f) * (0.58f * 1.05f / 2f))
    }
}
