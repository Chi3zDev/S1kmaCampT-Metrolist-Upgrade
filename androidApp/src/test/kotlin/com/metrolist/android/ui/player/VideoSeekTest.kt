package com.metrolist.android.ui.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VideoSeekTest {
    @Test
    fun seekIsClampedAndDisabledForLiveOrUnknownDuration() {
        assertEquals(25000L, videoDoubleTapSeek(15000, 40000, true, false))
        assertEquals(5000L, videoDoubleTapSeek(15000, 40000, false, false))
        assertEquals(0L, videoDoubleTapSeek(1000, 40000, false, false))
        assertEquals(40000L, videoDoubleTapSeek(39000, 40000, true, false))
        assertNull(videoDoubleTapSeek(10000, 40000, true, true))
        assertNull(videoDoubleTapSeek(10000, 0, true, false))
    }
}
