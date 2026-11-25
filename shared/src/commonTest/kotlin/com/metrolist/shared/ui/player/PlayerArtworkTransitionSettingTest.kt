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
    fun onlyGlassCardWithBothSwitchesOnDrawsPanel() {
        for (type in PlayerType.entries) {
            for (master in listOf(false, true)) {
                for (player in listOf(false, true)) {
                    for (lyrics in listOf(false, true)) {
                        val expected =
                            if (type == PlayerType.GLASS_CARD && master && player && !lyrics) {
                                PlayerPanelStyle.GLASS
                            } else {
                                PlayerPanelStyle.NONE
                            }
                        assertEquals(expected, playerPanelStyle(type, master, player, lyrics))
                    }
                }
            }
        }
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
