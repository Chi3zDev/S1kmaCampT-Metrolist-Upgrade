package com.metrolist.android.streak

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
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

    @Test
    fun restoresOneMissedDayWithoutChangingListeningHistory() {
        val now = Instant.parse("2026-01-05T12:00:00Z")
        val plays = listOf(ms("2026-01-01T12:00:00Z"), ms("2026-01-02T12:00:00Z"), ms("2026-01-03T12:00:00Z"))
        val ledger = applyStreakRecovery(plays, now, ZoneId.of("UTC"), StreakRecoveryLedger())!!
        assertEquals(setOf(LocalDate.parse("2026-01-04")), ledger.recoveredDays)
        assertEquals(4, ledger.remaining(LocalDate.parse("2026-01-05")))
        assertEquals(ListeningStreak(4, 4, false), calculateListeningStreak(plays, now, ZoneId.of("UTC"), ledger.recoveredDays))
        assertEquals(3, plays.size)
    }

    @Test
    fun rejectsTwoDayGapWhenOnlyOneRecoveryRemains() {
        val now = Instant.parse("2026-01-06T12:00:00Z")
        val plays = listOf(ms("2026-01-03T12:00:00Z"))
        val ledger = StreakRecoveryLedger(usageMonth = YearMonth.of(2026, 1), usedThisMonth = 4)
        assertEquals(StreakRecoveryFailure.INSUFFICIENT_QUOTA, planStreakRecovery(plays, now, ZoneId.of("UTC"), ledger).failure)
        assertNull(applyStreakRecovery(plays, now, ZoneId.of("UTC"), ledger))
    }

    @Test
    fun quotaResetsAtStartOfLocalMonth() {
        val ledger = StreakRecoveryLedger(usageMonth = YearMonth.of(2026, 1), usedThisMonth = 5)
        val plays = listOf(ms("2026-01-29T12:00:00Z"))
        val now = Instant.parse("2026-02-01T12:00:00Z")
        val next = applyStreakRecovery(plays, now, ZoneId.of("UTC"), ledger)
        assertNotNull(next)
        assertEquals(3, next!!.remaining(LocalDate.parse("2026-02-01")))
    }

    @Test
    fun rejectsGapLongerThanThreeDays() {
        val now = Instant.parse("2026-01-10T12:00:00Z")
        val plays = listOf(ms("2026-01-05T12:00:00Z"))
        assertEquals(StreakRecoveryFailure.GAP_TOO_LARGE, planStreakRecovery(plays, now, ZoneId.of("UTC"), StreakRecoveryLedger()).failure)
    }

    @Test
    fun recoveryReconnectsOldRunAfterListeningToday() {
        val now = Instant.parse("2026-01-05T12:00:00Z")
        val plays = listOf(ms("2026-01-01T12:00:00Z"), ms("2026-01-02T12:00:00Z"), ms("2026-01-05T08:00:00Z"))
        val ledger = applyStreakRecovery(plays, now, ZoneId.of("UTC"), StreakRecoveryLedger())!!
        assertEquals(setOf(LocalDate.parse("2026-01-03"), LocalDate.parse("2026-01-04")), ledger.recoveredDays)
        assertEquals(ListeningStreak(5, 5, true), calculateListeningStreak(plays, now, ZoneId.of("UTC"), ledger.recoveredDays))
    }

    @Test
    fun recoveryUsesLocalMidnightAndNeverRestoresToday() {
        val zone = ZoneId.of("Asia/Pontianak")
        val plays = listOf(ms("2026-01-01T16:00:00Z"))
        val before = Instant.parse("2026-01-02T16:59:00Z")
        val after = Instant.parse("2026-01-02T17:00:00Z")
        assertEquals(StreakRecoveryFailure.NO_BREAK, planStreakRecovery(plays, before, zone, StreakRecoveryLedger()).failure)
        assertEquals(listOf(LocalDate.parse("2026-01-02")), planStreakRecovery(plays, after, zone, StreakRecoveryLedger()).missingDays)
    }

    @Test
    fun oldBreakCannotBeRepairedAfterNewStreakHasContinued() {
        val plays = listOf(ms("2026-01-01T12:00:00Z"), ms("2026-01-05T12:00:00Z"), ms("2026-01-06T12:00:00Z"))
        val plan = planStreakRecovery(plays, Instant.parse("2026-01-06T15:00:00Z"), ZoneId.of("UTC"), StreakRecoveryLedger())
        assertEquals(StreakRecoveryFailure.NO_BREAK, plan.failure)
    }

    @Test
    fun recoveryLedgerRoundTripsAndCorruptionIsRejected() {
        val ledger = StreakRecoveryLedger(setOf(LocalDate.parse("2026-01-05")), YearMonth.of(2026, 1), 1)
        assertEquals(ledger, decodeStreakRecovery(encodeStreakRecovery(ledger)))
        assertNull(decodeStreakRecovery("not valid json"))
        assertNull(decodeStreakRecovery("{\"used\":7}"))
    }
}
