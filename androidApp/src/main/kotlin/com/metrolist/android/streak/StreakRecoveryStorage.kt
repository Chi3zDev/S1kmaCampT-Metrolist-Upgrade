package com.metrolist.android.streak

import com.metrolist.shared.settings.Settings
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class StoredStreakRecovery(
    val month: String? = null,
    val used: Int = 0,
    val restored: List<String> = emptyList(),
)

/** Null means invalid data; callers must not overwrite it with an empty ledger. */
fun decodeStreakRecovery(value: String): StreakRecoveryLedger? {
    if (value.isBlank()) return StreakRecoveryLedger()
    return runCatching {
        val record = Json.decodeFromString<StoredStreakRecovery>(value)
        require(record.used in 0..MONTHLY_STREAK_RECOVERIES)
        val month = record.month?.let(YearMonth::parse)
        require(month != null || record.used == 0)
        val dates = record.restored.map(LocalDate::parse)
        require(dates.distinct().size == dates.size)
        StreakRecoveryLedger(dates.toSet(), month, record.used)
    }.getOrNull()
}

fun encodeStreakRecovery(ledger: StreakRecoveryLedger): String =
    Json.encodeToString(
        StoredStreakRecovery(
            month = ledger.usageMonth?.toString(),
            used = ledger.usedThisMonth,
            restored = ledger.recoveredDays.sorted().map(LocalDate::toString),
        ),
    )

/** Update dates and quota together in one preference, without touching listening history. */
@Synchronized
fun persistStreakRecovery(timestamps: List<Long>, now: Instant, zone: ZoneId): Boolean {
    val settings = Settings.shared
    val ledger = decodeStreakRecovery(settings.streakRecoveryJson) ?: return false
    val updated = applyStreakRecovery(timestamps, now, zone, ledger) ?: return false
    settings.streakRecoveryJson = encodeStreakRecovery(updated)
    settings.flush()
    return true
}
