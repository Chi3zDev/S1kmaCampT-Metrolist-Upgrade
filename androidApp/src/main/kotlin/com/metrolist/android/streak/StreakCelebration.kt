package com.metrolist.android.streak

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.*
import com.metrolist.shared.settings.Settings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private data class Celebration(
    val today: LocalDate,
    val count: Int,
    val levelUp: Boolean,
    val provisional: Boolean,
    val listened: Set<LocalDate>,
    val restored: Set<LocalDate>,
)

/** Shows a provisional first-play celebration; qualified history is never modified here. */
@Composable
fun BoxScope.StreakCelebrationPresenter(
    database: MusicDatabase,
    isPlaying: Boolean,
    visible: Boolean,
    snackbarHostState: SnackbarHostState,
    onOpenStreak: () -> Unit,
) {
    val timestamps by remember(database) { database.dao().qualifyingStreakTimestamps() }
        .collectAsStateWithLifecycle(initialValue = null)
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val scope = rememberCoroutineScope()
    val banner = stringResource(Res.string.streak_recovery_invitation)
    val action = stringResource(Res.string.streak_restore)
    val recoveryJson = Settings.shared.streakRecoveryJson
    val celebrationEnabled = Settings.shared.streakCelebrationEnabled
    var celebration by remember { mutableStateOf<Celebration?>(null) }

    LaunchedEffect(timestamps, isPlaying, visible, lifecycle, recoveryJson, celebrationEnabled) {
        val plays = timestamps ?: return@LaunchedEffect
        if (!isPlaying || !visible || lifecycle != Lifecycle.State.RESUMED) return@LaunchedEffect
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        if (Settings.shared.streakCelebrationShownDate == today.toString()) return@LaunchedEffect
        val ledger = decodeStreakRecovery(recoveryJson) ?: return@LaunchedEffect
        val listened = plays.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.filter { !it.isAfter(today) }.toSet()
        val previous = (listened + ledger.recoveredDays).filter { it < today }.maxOrNull()
        val broken = previous != null && previous < today.minusDays(1)
        Settings.shared.streakCelebrationShownDate = today.toString()
        if (broken) {
            // No dialog or playback operation: a transient action merely opens the recovery page.
            scope.launch {
                if (snackbarHostState.showSnackbar(banner, actionLabel = action, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) onOpenStreak()
            }
        } else if (celebrationEnabled) {
            val yesterdayNoon = today.minusDays(1).atTime(12, 0).atZone(zone).toInstant()
            val previousCount = calculateListeningStreak(plays, yesterdayNoon, zone, ledger.recoveredDays).current
            val provisional = today !in listened
            val count = if (provisional) previousCount + 1 else calculateListeningStreak(plays, now, zone, ledger.recoveredDays).current
            celebration = Celebration(today, count, streakLevel(count).minimumDays > streakLevel(previousCount).minimumDays, provisional, listened, ledger.recoveredDays)
        }
    }
    LaunchedEffect(celebration) {
        if (celebration != null) { delay(8_000); celebration = null }
    }
    val event = celebration
    if (event != null && visible && lifecycle == Lifecycle.State.RESUMED) {
        val level = streakLevel(event.count)
        Card(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp).widthIn(max = 400.dp).zIndex(100f).clickable { celebration = null },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(if (event.levelUp) Res.string.streak_level_up else Res.string.streak_celebration_title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Flame(level.color, animate = streakMotionEnabled(), levelUp = event.levelUp)
                Text("${event.count} ${stringResource(Res.string.streak_days)}", style = MaterialTheme.typography.titleLarge)
                if (event.provisional) Text(stringResource(Res.string.streak_first_play_provisional), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                val start = event.today.minusDays((event.today.dayOfWeek.value - 1).toLong())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    repeat(7) { DayDot(start.plusDays(it.toLong()), event.today, event.listened, event.restored) }
                }
                TextButton(onClick = { celebration = null }) { Text(stringResource(Res.string.streak_celebration_continue)) }
            }
        }
    }
}
