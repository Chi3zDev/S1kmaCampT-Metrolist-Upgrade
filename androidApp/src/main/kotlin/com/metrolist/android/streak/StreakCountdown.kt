package com.metrolist.android.streak

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** A streak expires at local midnight after the last grace day, including DST changes. */
internal fun streakRemainingMinutes(
    streak: ListeningStreak,
    now: Instant,
    zone: ZoneId,
): Long? {
    if (streak.current <= 0) return null
    val today = now.atZone(zone).toLocalDate()
    val expiresAt = today.plusDays(if (streak.todayCounted) 2 else 1).atStartOfDay(zone).toInstant()
    val seconds = Duration.between(now, expiresAt).seconds.coerceAtLeast(0)
    return (seconds + 59) / 60
}

/** Keep the ring below complete until a qualifying event has actually been committed. */
internal fun streakTodayProgress(
    todayCounted: Boolean,
    listeningMs: Long?,
    minimumMs: Long,
): Float =
    when {
        todayCounted -> 1f
        listeningMs == null || minimumMs <= 0L -> 0f
        else -> (listeningMs.toFloat() / minimumMs).coerceIn(0f, 0.99f)
    }
