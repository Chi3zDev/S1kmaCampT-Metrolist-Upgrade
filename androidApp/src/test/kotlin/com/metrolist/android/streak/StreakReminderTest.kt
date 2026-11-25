package com.metrolist.android.streak

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class StreakReminderTest {
    @Test
    fun schedulesAtLocalTimeAndRollsToTomorrow() {
        val zone = ZoneId.of("Asia/Pontianak")
        assertEquals(60L * 60 * 1000, reminderDelayMillis(Instant.parse("2026-01-01T12:00:00Z"), zone, 1200))
        assertEquals(24L * 60 * 60 * 1000, reminderDelayMillis(Instant.parse("2026-01-01T13:00:00Z"), zone, 1200))
    }
}
