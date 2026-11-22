package com.metrolist.android.recap

import com.metrolist.shared.db.HistoryAlbumStatistic
import com.metrolist.shared.db.HistoryArtistStatistic
import com.metrolist.shared.db.HistoryStatGroup
import com.metrolist.shared.db.HistoryStatisticsSnapshot
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.db.mobileHistoryStatisticsSnapshot
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth

enum class RecapPeriod { YEAR, LAST_30_DAYS, MONTH, ALL }

data class RecapData(
    val minutes: Long,
    val songCount: Int,
    val artistCount: Int,
    val songs: List<HistoryStatGroup>,
    val artists: List<HistoryArtistStatistic>,
    val albums: List<HistoryAlbumStatistic>,
    val playCount: Int = 0,
)

fun recapRange(period: RecapPeriod, now: Instant, zone: ZoneId, month: YearMonth? = null): Pair<Long, Long> {
    val today = now.atZone(zone).toLocalDate()
    val start = when (period) {
        RecapPeriod.YEAR -> today.withDayOfYear(1)
        RecapPeriod.LAST_30_DAYS -> today.minusDays(29)
        RecapPeriod.MONTH -> (month ?: YearMonth.from(today)).atDay(1)
        RecapPeriod.ALL -> null
    }
    val end = if (period == RecapPeriod.MONTH && month != null) {
        minOf(now.toEpochMilli(), month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1)
    } else now.toEpochMilli()
    return (start?.atStartOfDay(zone)?.toInstant()?.toEpochMilli() ?: Long.MIN_VALUE) to end
}

fun recapData(snapshot: HistoryStatisticsSnapshot): RecapData = RecapData(
    minutes = snapshot.totals.playTimeMs / 60_000L,
    songCount = snapshot.songs.size,
    artistCount = snapshot.artists.size,
    songs = snapshot.songs.sortedByDescending { it.playTimeMs },
    artists = snapshot.artists.sortedByDescending { it.playTimeMs },
    albums = snapshot.albums.sortedByDescending { it.playTimeMs },
    playCount = snapshot.totals.playCount,
)

suspend fun MusicDatabase.loadRecap(period: RecapPeriod, now: Instant, zone: ZoneId, month: YearMonth? = null): RecapData {
    val (start, end) = recapRange(period, now, zone, month)
    return recapData(mobileHistoryStatisticsSnapshot(start, end))
}
