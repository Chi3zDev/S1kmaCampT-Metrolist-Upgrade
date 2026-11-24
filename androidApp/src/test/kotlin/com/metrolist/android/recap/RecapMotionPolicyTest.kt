package com.metrolist.android.recap

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecapMotionPolicyTest {
    @Test
    fun timerOnlyRunsWhenVisibleAndNotPausedOrHeld() {
        assertTrue(recapTimerRunning(visible = true, pressed = false, paused = false))
        assertFalse(recapTimerRunning(visible = false, pressed = false, paused = false))
        assertFalse(recapTimerRunning(visible = true, pressed = true, paused = false))
        assertFalse(recapTimerRunning(visible = true, pressed = false, paused = true))
    }
}
