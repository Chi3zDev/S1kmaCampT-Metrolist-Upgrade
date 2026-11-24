package com.metrolist.shared.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class AboutUpgradeNotesTest {
    @Test
    fun newerNotesAppearOnlyForNewerUpgradeWithoutHidingHistory() {
        assertEquals(listOf(1), upgradeNoteVersions(1))
        assertEquals(listOf(2, 1), upgradeNoteVersions(2))
        assertEquals(listOf(2, 1), upgradeNoteVersions(3))
    }
}
