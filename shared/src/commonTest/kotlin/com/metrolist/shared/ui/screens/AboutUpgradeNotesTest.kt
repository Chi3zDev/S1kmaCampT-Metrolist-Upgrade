package com.metrolist.shared.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class AboutUpgradeNotesTest {
    @Test
    fun candidateBadgeOnlyMatchesTheCandidateVersion() {
        assertEquals(true, isUpgradeCandidateTwo("14.0.1-upgrade.2-rc.2"))
        assertEquals(false, isUpgradeCandidateTwo("14.0.1-upgrade.2"))
        assertEquals(false, isUpgradeCandidateTwo("14.0.1-upgrade.2-rc.1"))
        assertEquals(true, isUpgradeCandidateThree("14.0.1-upgrade.2-rc.3"))
        assertEquals(false, isUpgradeCandidateThree("14.0.1-upgrade.2-rc.2"))
        assertEquals(true, isUpgradeCandidateFour("14.0.1-upgrade.2-rc.4"))
        assertEquals(false, isUpgradeCandidateFour("14.0.1-upgrade.2-rc.3"))
        assertEquals(true, isUpgradeCandidateFive("14.0.1-upgrade.2-rc.5"))
        assertEquals(false, isUpgradeCandidateFive("14.0.1-upgrade.2-rc.4"))
        assertEquals(true, isUpgradeCandidateSix("14.0.1-upgrade.2-rc.6"))
        assertEquals(false, isUpgradeCandidateSix("14.0.1-upgrade.2-rc.5"))
    }

    @Test
    fun newerNotesAppearOnlyForNewerUpgradeWithoutHidingHistory() {
        assertEquals(listOf(1), upgradeNoteVersions(1))
        assertEquals(listOf(2, 1), upgradeNoteVersions(2))
        assertEquals(listOf(2, 1), upgradeNoteVersions(3))
    }
}
