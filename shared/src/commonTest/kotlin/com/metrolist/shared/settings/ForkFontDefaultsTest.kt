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
}
