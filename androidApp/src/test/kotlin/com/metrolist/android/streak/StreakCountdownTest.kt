package com.metrolist.android.streak

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class StreakCountdownTest {
    @Test
    fun countedTodayExtendsThroughTomorrow() {
        assertEquals(
            36L * 60L,
            streakRemainingMinutes(ListeningStreak(2, 2, true), Instant.parse("2026-01-02T12:00:00Z"), ZoneId.of("UTC")),
        )
    }

    @Test
    fun uncountedTodayExpiresAtNextLocalMidnight() {
        assertEquals(
            60L,
            streakRemainingMinutes(ListeningStreak(2, 2, false), Instant.parse("2026-01-02T16:00:00Z"), ZoneId.of("Asia/Pontianak")),
        )
        assertNull(streakRemainingMinutes(ListeningStreak(0, 2, false), Instant.parse("2026-01-02T16:00:00Z"), ZoneId.of("UTC")))
    }

    @Test
    fun countdownUsesRealLocalMidnightsAcrossDaylightSaving() {
        assertEquals(
            23L * 60L,
            streakRemainingMinutes(ListeningStreak(1, 1, false), Instant.parse("2026-03-08T05:00:00Z"), ZoneId.of("America/New_York")),
        )
    }
}
