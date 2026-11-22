package com.metrolist.android.recap

import com.metrolist.shared.db.HistoryArtistStatistic
import com.metrolist.shared.db.HistoryStatGroup
import com.metrolist.shared.db.HistoryStatisticsSnapshot
import com.metrolist.shared.db.HistoryTotals
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class RecapDataTest {
    private fun song(id: String, duration: Long) = HistoryStatGroup(
        id, id, "Artist", null, null, null, null, false, 1, duration, 0, 0, null,
    )

    @Test
    fun emptyPeriod() {
        val recap = recapData(HistoryStatisticsSnapshot(emptyList(), emptyList(), emptyList(), HistoryTotals(0, 0), emptyList(), emptyList()))
        assertEquals(0L, recap.minutes)
        assertEquals(0, recap.songCount)
        assertEquals(0, recap.artistCount)
    }

    @Test
    fun durationDeterminesMinutesAndRanking() {
        val snapshot = HistoryStatisticsSnapshot(
            listOf(song("short", 30_000), song("long", 90_000)),
            listOf(HistoryArtistStatistic("Short", 1, 30_000, null), HistoryArtistStatistic("Long", 1, 90_000, null)),
            emptyList(), HistoryTotals(2, 120_000), emptyList(), emptyList(),
        )
        val recap = recapData(snapshot)
        assertEquals(2L, recap.minutes)
        assertEquals("long", recap.songs.first().songId)
        assertEquals("Long", recap.artists.first().name)
    }

    @Test
    fun previousYearRangeExcludesEarlierYears() {
        val (start, end) = recapRange(RecapPeriod.YEAR, Instant.parse("2025-12-31T10:00:00Z"), ZoneId.of("UTC"))
        assertEquals(Instant.parse("2025-01-01T00:00:00Z").toEpochMilli(), start)
        assertEquals(Instant.parse("2025-12-31T10:00:00Z").toEpochMilli(), end)
    }

    @Test
    fun newYearUsesLocalMidnight() {
        val (start, end) = recapRange(RecapPeriod.YEAR, Instant.parse("2026-12-31T17:30:00Z"), ZoneId.of("Asia/Pontianak"))
        assertEquals(Instant.parse("2026-12-31T17:00:00Z").toEpochMilli(), start)
        assertEquals(Instant.parse("2026-12-31T17:30:00Z").toEpochMilli(), end)
    }
}
