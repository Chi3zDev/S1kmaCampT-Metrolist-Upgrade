package com.metrolist.shared.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class AboutUpgradeNotesTest {
    @Test
    fun candidateBadgeOnlyMatchesTheCandidateVersion() {
        assertEquals(true, isUpgradeCandidateTwo("14.0.1-upgrade.2-rc.2"))
        assertEquals(false, isUpgradeCandidateTwo("14.0.1-upgrade.2"))
        assertEquals(false, isUpgradeCandidateTwo("14.0.1-upgrade.2-rc.1"))
    }

    @Test
    fun newerNotesAppearOnlyForNewerUpgradeWithoutHidingHistory() {
        assertEquals(listOf(1), upgradeNoteVersions(1))
        assertEquals(listOf(2, 1), upgradeNoteVersions(2))
        assertEquals(listOf(2, 1), upgradeNoteVersions(3))
    }
}
