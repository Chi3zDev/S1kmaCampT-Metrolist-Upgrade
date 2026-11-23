package com.metrolist.android.ui.player

import org.junit.Assert.assertEquals
import org.junit.Test

class SystemMusicVolumeControlTest {
    @Test
    fun volumeConvertsAndClamps() {
        assertEquals(0.5f, systemVolumeFraction(5, 0, 10), 0.0001f)
        assertEquals(0f, systemVolumeFraction(0, 0, 0), 0f)
        assertEquals(5, systemVolumeFromFraction(0.5f, 0, 10))
        assertEquals(10, systemVolumeFromFraction(2f, 0, 10))
        assertEquals(2, systemVolumeFromFraction(Float.NaN, 2, 8))
    }
}
