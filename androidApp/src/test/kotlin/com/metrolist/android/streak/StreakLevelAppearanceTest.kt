package com.metrolist.android.streak

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakLevelAppearanceTest {
    @Test
    fun eachExistingThresholdHasItsOwnFlameAndColor() {
        assertEquals(listOf(1, 3, 7, 14, 30, 60, 100), streakLevels.map { it.minimumDays })
        assertEquals(streakLevels.size, streakLevels.map { it.flameVariant }.distinct().size)
        assertEquals(streakLevels.size, streakLevels.map { it.color }.distinct().size)
        streakLevels.forEach { level -> assertEquals(level, streakLevel(level.minimumDays)) }
    }
}
