package com.metrolist.android.streak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.settings.Settings
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.metrolist.android.R
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.streak_active
import com.metrolist.shared.generated.resources.streak_days
import com.metrolist.shared.generated.resources.streak_inactive
import com.metrolist.shared.generated.resources.streak_level_middle
import com.metrolist.shared.generated.resources.streak_level_start
import com.metrolist.shared.generated.resources.streak_level_top
import com.metrolist.shared.generated.resources.streak_longest
import com.metrolist.shared.generated.resources.streak_next
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberListeningStreak(database: MusicDatabase): ListeningStreak {
    val timestamps by remember(database) { database.dao().qualifyingStreakTimestamps() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Instant.now()
            delay(60_000)
        }
    }
    val recoveryJson = Settings.shared.streakRecoveryJson
    val restoredDays = remember(recoveryJson) { decodeStreakRecovery(recoveryJson)?.recoveredDays.orEmpty() }
    return remember(timestamps, now, restoredDays) { calculateListeningStreak(timestamps, now, ZoneId.systemDefault(), restoredDays) }
}

@Composable
fun StreakCard(streak: ListeningStreak, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val currentLevel = streakLevel(streak.current)
    val nextLevel = streakLevels.firstOrNull { it.minimumDays > streak.current }
    val level = stringResource(currentLevel.title)
    val progress = if (nextLevel == null) 1f else
        ((streak.current - currentLevel.minimumDays).toFloat() / (nextLevel.minimumDays - currentLevel.minimumDays)).coerceIn(0f, 1f)
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh)), border = glassCardBorder()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(painterResource(R.drawable.ic_streak_flame), contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(streak.current.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(stringResource(Res.string.streak_days), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(level, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(stringResource(if (streak.todayCounted) Res.string.streak_active else Res.string.streak_inactive), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (nextLevel != null) Text(stringResource(Res.string.streak_next, stringResource(nextLevel.title)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Text(stringResource(Res.string.streak_longest, streak.longest), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
