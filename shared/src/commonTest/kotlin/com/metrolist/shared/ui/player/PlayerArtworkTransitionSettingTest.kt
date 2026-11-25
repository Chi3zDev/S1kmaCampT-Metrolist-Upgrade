package com.metrolist.shared.ui.player

import com.metrolist.shared.settings.PlayerType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerArtworkTransitionSettingTest {
    @Test
    fun lyricsTransitionRespectsVisibilityAndLightweightModes() {
        assertTrue(playerLyricsMotionAllowed(visible = true, lightMode = false, deviceAllowed = true))
        assertFalse(playerLyricsMotionAllowed(visible = false, lightMode = false, deviceAllowed = true))
        assertFalse(playerLyricsMotionAllowed(visible = true, lightMode = true, deviceAllowed = true))
        assertFalse(playerLyricsMotionAllowed(visible = true, lightMode = false, deviceAllowed = false))
        assertTrue(playerDecorativeMotionAllowed(visible = true, lightMode = false, deviceAllowed = true))
        assertFalse(playerDecorativeMotionAllowed(visible = false, lightMode = false, deviceAllowed = true))
        assertFalse(playerDecorativeMotionAllowed(visible = true, lightMode = true, deviceAllowed = true))
        assertFalse(playerDecorativeMotionAllowed(visible = true, lightMode = false, deviceAllowed = false))
    }

    @Test
    fun panelsRemainWhenGlassIsOffAndVinylHasNoCard() {
        for (type in PlayerType.entries.filter { it != PlayerType.VINYL }) {
            assertEquals(PlayerPanelStyle.GLASS, playerPanelStyle(type, true, true, false))
            assertEquals(PlayerPanelStyle.SOLID, playerPanelStyle(type, false, true, false))
            assertEquals(PlayerPanelStyle.SOLID, playerPanelStyle(type, true, false, false))
            assertEquals(PlayerPanelStyle.NONE, playerPanelStyle(type, true, true, true))
        }
        assertEquals(PlayerPanelStyle.NONE, playerPanelStyle(PlayerType.VINYL, true, true, false))
        assertEquals(PlayerPanelStyle.NONE, playerPanelStyle(PlayerType.VINYL, false, false, false))
    }

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
