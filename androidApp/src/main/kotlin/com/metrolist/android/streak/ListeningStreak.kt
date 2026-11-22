package com.metrolist.android.streak

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ListeningStreak(val current: Int, val longest: Int, val todayCounted: Boolean)

fun calculateListeningStreak(timestamps: List<Long>, now: Instant, zone: ZoneId): ListeningStreak {
    val today = now.atZone(zone).toLocalDate()
    val days = timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        .filter { !it.isAfter(today) }.distinct().sorted()
    var longest = 0
    var run = 0
    var previous: LocalDate? = null
    for (day in days) {
        run = if (previous?.plusDays(1) == day) run + 1 else 1
        longest = maxOf(longest, run)
        previous = day
    }
    val todayCounted = previous == today
    val current = if (todayCounted || previous == today.minusDays(1)) run else 0
    return ListeningStreak(current, longest, todayCounted)
}
