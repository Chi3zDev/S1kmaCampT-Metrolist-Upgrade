package com.metrolist.android.streak

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class ListeningStreakTest {
    private fun ms(value: String) = Instant.parse(value).toEpochMilli()

    @Test
    fun consecutiveDaysAndDuplicatePlays() {
        val plays = listOf(ms("2026-01-01T12:00:00Z"), ms("2026-01-02T12:00:00Z"), ms("2026-01-02T13:00:00Z"))
        assertEquals(ListeningStreak(2, 2, true), calculateListeningStreak(plays, Instant.parse("2026-01-02T15:00:00Z"), ZoneId.of("UTC")))
    }

    @Test
    fun gapResetsCurrentButKeepsLongest() {
        val plays = listOf(ms("2026-01-01T12:00:00Z"), ms("2026-01-02T12:00:00Z"), ms("2026-01-05T12:00:00Z"))
        assertEquals(ListeningStreak(1, 2, false), calculateListeningStreak(plays, Instant.parse("2026-01-06T12:00:00Z"), ZoneId.of("UTC")))
        assertEquals(ListeningStreak(0, 2, false), calculateListeningStreak(plays, Instant.parse("2026-01-07T12:00:00Z"), ZoneId.of("UTC")))
    }

    @Test
    fun localMidnightUsesDeviceZone() {
        val plays = listOf(ms("2026-01-01T16:59:00Z"), ms("2026-01-01T17:01:00Z"))
        assertEquals(ListeningStreak(2, 2, true), calculateListeningStreak(plays, Instant.parse("2026-01-01T18:00:00Z"), ZoneId.of("Asia/Pontianak")))
        assertEquals(ListeningStreak(1, 1, true), calculateListeningStreak(plays, Instant.parse("2026-01-01T18:00:00Z"), ZoneId.of("UTC")))
    }
}
