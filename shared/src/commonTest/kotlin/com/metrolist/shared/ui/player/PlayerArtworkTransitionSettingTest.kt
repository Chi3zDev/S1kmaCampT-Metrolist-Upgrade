package com.metrolist.shared.ui.player

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerArtworkTransitionSettingTest {
    @Test
    fun disabledTransitionsSnapToCurrentArtworkEvenDuringAudioCrossfade() {
        val crossfade = PlayerArtworkCrossfade("next") { 0.4f }
        assertEquals(PlayerBackgroundTransition.Snap("current"), playerBackgroundTransition("current", "old", crossfade, false))
        assertEquals(PlayerBackgroundTransition.Seek("next", 0.4f), playerBackgroundTransition("current", "old", crossfade, true))
    }

    @Test
    fun normalArtworkChangeAnimatesOnlyWhenAllowed() {
        val noCrossfade = PlayerArtworkCrossfade()
        assertEquals(PlayerBackgroundTransition.Animate("current"), playerBackgroundTransition("current", "old", noCrossfade, true))
        assertEquals(PlayerBackgroundTransition.Snap("current"), playerBackgroundTransition("current", "old", noCrossfade, false))
    }
}
