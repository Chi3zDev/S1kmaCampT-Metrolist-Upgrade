package com.metrolist.shared.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlassScrollMotionTest {
    @Test
    fun slowAndFastConsumedScrollReachSameEndpoint() {
        val fast = updateGlassScrollMotion(GlassScrollMotion(), -64f, 0f, 64f)
        var slow = GlassScrollMotion()
        repeat(4) { slow = updateGlassScrollMotion(slow, -16f, 0f, 64f) }
        assertTrue(fast.compact)
        assertEquals(fast, slow)
    }

    @Test
    fun topOverscrollExpandsWithoutCountingUnconsumedMovement() {
        val compact = GlassScrollMotion(compact = true)
        assertFalse(updateGlassScrollMotion(compact, 0f, 20f, 64f).compact)
        assertEquals(GlassScrollMotion(), updateGlassScrollMotion(GlassScrollMotion(), 0f, -100f, 64f))
    }

    @Test
    fun reverseScrollInsideListKeepsBarCompact() {
        val compact = GlassScrollMotion(compact = true)
        val reversed = updateGlassScrollMotion(compact, 80f, 0f, 64f)
        assertTrue(reversed.compact)
        assertEquals(0f, reversed.distance)
    }

    @Test
    fun reachingTopDuringConsumedScrollExpandsBar() {
        val compact = GlassScrollMotion(compact = true)
        assertEquals(GlassScrollMotion(), updateGlassScrollMotion(compact, 10f, 5f, 64f))
    }

    @Test
    fun reversalResetsAccumulatedDistance() {
        val down = updateGlassScrollMotion(GlassScrollMotion(), -50f, 0f, 64f)
        val reverse = updateGlassScrollMotion(down, 20f, 0f, 64f)
        assertEquals(20f, reverse.distance)
        assertFalse(reverse.compact)
    }
}
