package com.metrolist.android.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneralVideoModeTest {
    @Test
    fun returningFromGeneralVideoToMusicResetsVideoMode() {
        assertTrue(shouldResetVideoAfterGeneralVideo(wasGeneralVideo = true, nextGeneralVideo = false, startVideo = false))
        assertFalse(shouldResetVideoAfterGeneralVideo(wasGeneralVideo = true, nextGeneralVideo = true, startVideo = true))
        assertFalse(shouldResetVideoAfterGeneralVideo(wasGeneralVideo = true, nextGeneralVideo = false, startVideo = true))
        assertFalse(shouldResetVideoAfterGeneralVideo(wasGeneralVideo = false, nextGeneralVideo = false, startVideo = false))
    }
}
