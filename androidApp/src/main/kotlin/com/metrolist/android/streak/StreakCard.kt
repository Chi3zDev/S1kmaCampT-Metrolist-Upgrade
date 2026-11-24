package com.metrolist.android.streak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.streak_active
import com.metrolist.shared.generated.resources.streak_days
import com.metrolist.shared.generated.resources.streak_extinguished
import com.metrolist.shared.generated.resources.streak_inactive
import com.metrolist.shared.generated.resources.streak_longest
import com.metrolist.shared.generated.resources.streak_next
import com.metrolist.shared.generated.resources.streak_previous
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.ui.navigation.LocalContentObscured
import com.metrolist.shared.ui.navigation.LocalRootContentActive
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import java.time.Instant
import java.time.ZoneId

data class StreakCardState(
    val streak: ListeningStreak,
    val lastStreak: Int,
)

@Composable
fun rememberListeningStreak(database: MusicDatabase): StreakCardState {
    val timestamps by remember(database) { database.dao().qualifyingStreakTimestamps() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var now by remember { mutableStateOf(Instant.now()) }
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val active = lifecycle == Lifecycle.State.RESUMED && LocalRootContentActive.current && !LocalContentObscured.current
    LaunchedEffect(active) {
        while (active) {
            now = Instant.now()
            delay(60_000)
        }
    }
    val recoveryJson = Settings.shared.streakRecoveryJson
    val restoredDays = remember(recoveryJson) { decodeStreakRecovery(recoveryJson)?.recoveredDays.orEmpty() }
    return remember(timestamps, now, restoredDays) {
        val zone = ZoneId.systemDefault()
        StreakCardState(
            calculateListeningStreak(timestamps, now, zone, restoredDays),
            lastListeningStreak(timestamps, now, zone, restoredDays),
        )
    }
}

@Composable
fun StreakCard(
    state: StreakCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val streak = state.streak
    val extinguished = state.lastStreak > 0 && streak.current == 0
    val flameTint = if (extinguished) MaterialTheme.colorScheme.outline else streakLevel(streak.current).color
    val currentLevel = streakLevel(if (extinguished) state.lastStreak else streak.current)
    val nextLevel = if (extinguished) null else streakLevels.firstOrNull { it.minimumDays > streak.current }
    val level = stringResource(currentLevel.title)
    val progress =
        if (extinguished) {
            0f
        } else if (nextLevel == null) {
            1f
        } else {
            ((streak.current - currentLevel.minimumDays).toFloat() / (nextLevel.minimumDays - currentLevel.minimumDays)).coerceIn(0f, 1f)
        }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = glassCardBorder(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
                val showMotion =
                    lifecycle == Lifecycle.State.RESUMED && LocalRootContentActive.current && !LocalContentObscured.current &&
                        streakMotionEnabled()
                Flame(
                    flameTint,
                    animate = showMotion,
                    particles = !extinguished,
                    extinguished = extinguished,
                    modifier = Modifier.size(40.dp),
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StreakCount(streak.current, showMotion, MaterialTheme.typography.headlineMedium)
                        Text(
                            stringResource(Res.string.streak_days),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(level, style = MaterialTheme.typography.labelLarge, color = flameTint)
                }
            }
            Text(
                stringResource(
                    when {
                        extinguished -> Res.string.streak_extinguished
                        streak.todayCounted -> Res.string.streak_active
                        else -> Res.string.streak_inactive
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (extinguished) Text(stringResource(Res.string.streak_previous, state.lastStreak), style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (nextLevel !=
                    null
                ) {
                    Text(
                        stringResource(Res.string.streak_next, stringResource(nextLevel.title)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LinearProgressIndicator(progress = {
                progress
            }, modifier = Modifier.fillMaxWidth(), color = flameTint, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Text(
                stringResource(Res.string.streak_longest, streak.longest),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
