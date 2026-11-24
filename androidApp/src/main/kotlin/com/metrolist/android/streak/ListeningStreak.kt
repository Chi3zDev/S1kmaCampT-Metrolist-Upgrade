package com.metrolist.android.streak

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class ListeningStreak(
    val current: Int,
    val longest: Int,
    val todayCounted: Boolean,
)

fun calculateListeningStreak(
    timestamps: List<Long>,
    now: Instant,
    zone: ZoneId,
    restoredDays: Set<LocalDate> = emptySet(),
): ListeningStreak {
    val today = now.atZone(zone).toLocalDate()
    val listenedDays = timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.toSet()
    val days = (listenedDays + restoredDays).filter { !it.isAfter(today) }.sorted()
    var longest = 0
    var run = 0
    var previous: LocalDate? = null
    for (day in days) {
        run = if (previous?.plusDays(1) == day) run + 1 else 1
        longest = maxOf(longest, run)
        previous = day
    }
    val todayCounted = today in listenedDays
    val current = if (previous == today || previous == today.minusDays(1)) run else 0
    return ListeningStreak(current, longest, todayCounted)
}

const val MONTHLY_STREAK_RECOVERIES = 5
const val MAX_STREAK_GAP_DAYS = 3

data class StreakRecoveryLedger(
    val recoveredDays: Set<LocalDate> = emptySet(),
    val usageMonth: YearMonth? = null,
    val usedThisMonth: Int = 0,
) {
    fun remaining(today: LocalDate): Int =
        (MONTHLY_STREAK_RECOVERIES - if (usageMonth == YearMonth.from(today)) usedThisMonth.coerceAtLeast(0) else 0)
            .coerceIn(0, MONTHLY_STREAK_RECOVERIES)
}

enum class StreakRecoveryFailure { NO_BREAK, GAP_TOO_LARGE, INSUFFICIENT_QUOTA }

data class StreakRecoveryPlan(
    val missingDays: List<LocalDate>,
    val remaining: Int,
    val failure: StreakRecoveryFailure?,
) {
    val canRecover: Boolean get() = failure == null
}

/** Find the last interrupted run; today is not a missed full day. */
fun planStreakRecovery(
    timestamps: List<Long>,
    now: Instant,
    zone: ZoneId,
    ledger: StreakRecoveryLedger,
): StreakRecoveryPlan {
    val today = now.atZone(zone).toLocalDate()
    val days =
        (timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } + ledger.recoveredDays)
            .filter { !it.isAfter(today) }
            .distinct()
            .sorted()
    val remaining = ledger.remaining(today)
    if (days.isEmpty()) return StreakRecoveryPlan(emptyList(), remaining, StreakRecoveryFailure.NO_BREAK)
    var start = days.lastIndex
    while (start > 0 && days[start - 1].plusDays(1) == days[start]) start--
    val previous: LocalDate
    val next: LocalDate
    if (days.last() < today.minusDays(1)) {
        previous = days.last()
        next = today
    } else {
        // Reconnect only the fresh streak started today. Older gaps must not be
        // recoverable indefinitely after a new streak has already continued.
        if (start == 0 || days[start] != today) return StreakRecoveryPlan(emptyList(), remaining, StreakRecoveryFailure.NO_BREAK)
        previous = days[start - 1]
        next = days[start]
    }
    val gap =
        java.time.temporal.ChronoUnit.DAYS
            .between(previous, next) - 1
    if (gap > MAX_STREAK_GAP_DAYS) return StreakRecoveryPlan(emptyList(), remaining, StreakRecoveryFailure.GAP_TOO_LARGE)
    val missing = (1..gap.toInt()).map { previous.plusDays(it.toLong()) }
    val failure =
        when {
            missing.isEmpty() -> StreakRecoveryFailure.NO_BREAK
            missing.size > remaining -> StreakRecoveryFailure.INSUFFICIENT_QUOTA
            else -> null
        }
    return StreakRecoveryPlan(missing, remaining, failure)
}

/** Callers must persist the returned ledger atomically; the original play history stays unchanged. */
fun applyStreakRecovery(
    timestamps: List<Long>,
    now: Instant,
    zone: ZoneId,
    ledger: StreakRecoveryLedger,
): StreakRecoveryLedger? {
    val plan = planStreakRecovery(timestamps, now, zone, ledger)
    if (!plan.canRecover) return null
    val month = YearMonth.from(now.atZone(zone))
    val used = if (ledger.usageMonth == month) ledger.usedThisMonth.coerceIn(0, MONTHLY_STREAK_RECOVERIES) else 0
    return ledger.copy(
        recoveredDays = ledger.recoveredDays + plan.missingDays,
        usageMonth = month,
        usedThisMonth =
            used + plan.missingDays.size,
    )
}

/** Preview the first play without inserting an artificial history entry. */
data class FirstPlayStreak(
    val broken: Boolean,
    val provisional: Boolean,
    val count: Int,
    val previousCount: Int,
)

fun firstPlayStreak(
    timestamps: List<Long>,
    now: Instant,
    zone: ZoneId,
    restoredDays: Set<LocalDate> = emptySet(),
): FirstPlayStreak {
    val today = now.atZone(zone).toLocalDate()
    val listened =
        timestamps
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .filter { !it.isAfter(today) }
            .toSet()
    val last = (listened + restoredDays).filter { it < today }.maxOrNull()
    val broken = last != null && last < today.minusDays(1)
    val yesterdayNoon =
        today
            .minusDays(1)
            .atTime(12, 0)
            .atZone(zone)
            .toInstant()
    val previousCount = calculateListeningStreak(timestamps, yesterdayNoon, zone, restoredDays).current
    val provisional = today !in listened
    val count = if (provisional) previousCount + 1 else calculateListeningStreak(timestamps, now, zone, restoredDays).current
    return FirstPlayStreak(broken, provisional, count, previousCount)
}

/** Length of the most recent completed or active run, even when it is now extinguished. */
fun lastListeningStreak(
    timestamps: List<Long>,
    now: Instant,
    zone: ZoneId,
    restoredDays: Set<LocalDate> = emptySet(),
): Int {
    val today = now.atZone(zone).toLocalDate()
    val days =
        (timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } + restoredDays)
            .filter { !it.isAfter(today) }
            .toSet()
    var day = days.maxOrNull() ?: return 0
    var length = 0
    while (day in days) {
        length++
        day = day.minusDays(1)
    }
    return length
}
