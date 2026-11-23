package com.metrolist.shared.settings

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class ForkFontDefaultsTest {
    @Test
    fun freshInstallGetsBundledFontOnlyWhenNoFontWasStored() {
        val fresh = Settings(MapSettings())
        fresh.applyFreshAndroidFontDefault(true)
        assertEquals("nunito_extrabold", fresh.androidActiveFontId)
        fresh.androidActiveFontId = "SYSTEM"
        fresh.applyFreshAndroidFontDefault(true)
        assertEquals("SYSTEM", fresh.androidActiveFontId)
    }

    @Test
    fun existingInstallKeepsItsDefaultAndExistingSelections() {
        val existing = Settings(MapSettings())
        existing.applyFreshAndroidFontDefault(false)
        assertEquals("", existing.androidActiveFontId)
        val imported = Settings(MapSettings("androidCustomFontFile" to "font.ttf"))
        imported.applyFreshAndroidFontDefault(true)
        assertEquals("", imported.androidActiveFontId)
        assertEquals("font.ttf", imported.androidCustomFontFile)
    }
    @Test
    fun freshAppearanceDefaultsDoNotOverwriteStoredChoices() {
        val fresh = Settings(MapSettings())
        fresh.applyFreshAndroidAppearanceDefaults(true)
        assertEquals(PlayerType.VINTAGE, fresh.playerType)
        assertEquals(PlayerThumbnailStyle.FULLSCREEN, fresh.playerThumbnailStyle)
        assertEquals(PlayerBackgroundStyle.LIQUID, fresh.playerBackgroundStyle)
        assertEquals(SliderStyle.SLIM, fresh.sliderStyle)
        assertEquals(3f, fresh.sliderTrackThicknessDp)
        assertEquals(MiniPlayerType.MODERN, fresh.miniPlayerType)
        assertEquals("BLURRED_ARTWORK", fresh.miniPlayerBackgroundStyle)
        assertEquals(true, fresh.glassNavigation)
        assertEquals("CLEAR", fresh.glassStyle)
        assertEquals(true, fresh.lyricsGlowEnabled)
        assertEquals(true, fresh.playerAuraEnabled)
        assertEquals(ThemePaletteStyle.CONTENT, fresh.themePaletteStyle)

        fresh.playerType = PlayerType.VINYL
        fresh.glassStyle = "FROSTED"
        fresh.lyricsGlowEnabled = false
        fresh.playerAuraEnabled = false
        fresh.applyFreshAndroidAppearanceDefaults(true)
        assertEquals(PlayerType.VINYL, fresh.playerType)
        assertEquals("FROSTED", fresh.glassStyle)
        assertEquals(false, fresh.lyricsGlowEnabled)
        assertEquals(false, fresh.playerAuraEnabled)
        val existing = Settings(MapSettings())
        existing.applyFreshAndroidAppearanceDefaults(false)
        assertEquals(PlayerType.MODERN, existing.playerType)
        assertEquals(false, existing.glassNavigation)
    }
}
