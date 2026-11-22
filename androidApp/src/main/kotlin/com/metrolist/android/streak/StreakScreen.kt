package com.metrolist.android.streak

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.metrolist.android.LocalMiniPlayerBottomInset
import com.metrolist.android.ui.component.tabContentTopPadding
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.*
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.ui.navigation.LocalRootContentActive
import com.metrolist.shared.ui.navigation.LocalContentObscured
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun StreakScreen(onBack: () -> Unit) {
    val database: MusicDatabase = koinInject()
    val timestamps by remember(database) { database.dao().qualifyingStreakTimestamps() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val screenActive = lifecycle == Lifecycle.State.RESUMED && LocalRootContentActive.current && !LocalContentObscured.current
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(screenActive) {
        while (screenActive) {
            now = Instant.now()
            delay(30_000)
        }
    }
    val zone = ZoneId.systemDefault()
    val today = now.atZone(zone).toLocalDate()
    val recoveryJson = Settings.shared.streakRecoveryJson
    val ledger = remember(recoveryJson) { decodeStreakRecovery(recoveryJson) }
    val restored = ledger?.recoveredDays.orEmpty()
    val listened = remember(timestamps, zone, today) { timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.filter { !it.isAfter(today) }.toSet() }
    val streak = remember(timestamps, now, zone, restored) { calculateListeningStreak(timestamps, now, zone, restored) }
    val plan = remember(timestamps, now, zone, ledger) { ledger?.let { planStreakRecovery(timestamps, now, zone, it) } }
    val allDays = (listened + restored).filter { !it.isAfter(today) }.toSet()
    val latest = allDays.maxOrNull()
    val extinguished = latest != null && latest.isBefore(today.minusDays(1))
    val previousLength = remember(allDays, latest) {
        var day = latest
        var count = 0
        while (day != null && day in allDays) { count++; day = day.minusDays(1) }
        count
    }
    val shownLevel = streakLevel(if (extinguished) previousLength else streak.current)
    val flameColor = if (extinguished) MaterialTheme.colorScheme.outline else shownLevel.color
    val next = streakLevels.firstOrNull { it.minimumDays > streak.current }
    val progress = if (next == null) 1f else ((streak.current - shownLevel.minimumDays).toFloat() / (next.minimumDays - shownLevel.minimumDays)).coerceIn(0f, 1f)
    val scope = rememberCoroutineScope()
    var confirmation by remember { mutableStateOf(false) }
    var restoreFailed by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = tabContentTopPadding(), bottom = LocalMiniPlayerBottomInset.current + 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TextButton(onClick = onBack) { Text("‹  " + stringResource(Res.string.back)) } }
        item { Text(stringResource(Res.string.streak_page_title), style = MaterialTheme.typography.headlineMedium) }
        item {
            StreakPanel {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Flame(flameColor, animate = !extinguished && screenActive && streakMotionEnabled())
                    Text(streak.current.toString(), style = MaterialTheme.typography.displayMedium)
                    Text(stringResource(shownLevel.title), color = flameColor, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(when { extinguished -> Res.string.streak_extinguished; streak.todayCounted -> Res.string.streak_active; else -> Res.string.streak_waiting }), textAlign = TextAlign.Center)
                    if (extinguished) Text(stringResource(Res.string.streak_previous, previousLength))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = flameColor)
                    if (next != null) Text(stringResource(Res.string.streak_next, stringResource(next.title)), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            StreakPanel {
                Text(stringResource(Res.string.streak_week), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.streak_calendar_legend), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(7) { offset ->
                        val day = start.plusDays(offset.toLong())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault()), style = MaterialTheme.typography.labelSmall)
                            DayDot(day, today, listened, restored)
                        }
                    }
                }
            }
        }
        item {
            StreakPanel {
                val month = YearMonth.from(today)
                Text(stringResource(Res.string.streak_calendar), style = MaterialTheme.typography.titleMedium)
                Text("${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                val blanks = month.atDay(1).dayOfWeek.value - 1
                val rows = (blanks + month.lengthOfMonth() + 6) / 7
                repeat(rows) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { col ->
                            val day = row * 7 + col - blanks + 1
                            if (day in 1..month.lengthOfMonth()) DayDot(month.atDay(day), today, listened, restored)
                            else Spacer(Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
        item {
            StreakPanel {
                Text(stringResource(Res.string.streak_longest, streak.longest))
                Text(stringResource(Res.string.streak_total_days, listened.size))
                if (restoreFailed) Text(stringResource(Res.string.streak_restore_failed), color = MaterialTheme.colorScheme.error)
                if (ledger == null) Text(stringResource(Res.string.streak_restore_corrupt), color = MaterialTheme.colorScheme.error)
                else {
                    Text(stringResource(Res.string.streak_recovery_remaining, ledger.remaining(today), MONTHLY_STREAK_RECOVERIES))
                    when (plan?.failure) {
                        StreakRecoveryFailure.GAP_TOO_LARGE -> Text(stringResource(Res.string.streak_restore_too_long))
                        StreakRecoveryFailure.INSUFFICIENT_QUOTA -> Text(stringResource(Res.string.streak_restore_no_quota))
                        else -> Unit
                    }
                    if (plan?.canRecover == true) Button(onClick = { confirmation = true }) { Text(stringResource(Res.string.streak_restore)) }
                }
            }
        }
    }
    if (confirmation && plan != null) {
        AlertDialog(onDismissRequest = { confirmation = false }, title = { Text(stringResource(Res.string.streak_restore)) },
            text = { Text(stringResource(Res.string.streak_restore_confirm, plan.missingDays.size, plan.remaining - plan.missingDays.size)) },
            confirmButton = { TextButton(onClick = {
                confirmation = false
                scope.launch {
                    val freshTimestamps = database.dao().qualifyingStreakTimestamps().first()
                    restoreFailed = !withContext(Dispatchers.IO) {
                        runCatching { persistStreakRecovery(freshTimestamps, Instant.now(), ZoneId.systemDefault()) }.getOrDefault(false)
                    }
                }
            }) { Text(stringResource(Res.string.streak_restore)) } },
            dismissButton = { TextButton(onClick = { confirmation = false }) { Text(stringResource(Res.string.cancel)) } })
    }
}

@Composable
private fun StreakPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh)), border = glassCardBorder()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
internal fun DayDot(day: LocalDate, today: LocalDate, listened: Set<LocalDate>, restored: Set<LocalDate>) {
    val c = MaterialTheme.colorScheme
    val background = when { day in listened -> c.primary; day in restored -> c.tertiary; day == today -> c.secondaryContainer; else -> c.surfaceVariant }
    val foreground = when { day in listened -> c.onPrimary; day in restored -> c.onTertiary; else -> c.onSurfaceVariant }
    Box(Modifier.size(36.dp).padding(2.dp).background(background, CircleShape)
        .then(if (day == today) Modifier.border(2.dp, c.outline, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
        Text(day.dayOfMonth.toString(), color = foreground, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun Flame(color: Color, animate: Boolean, levelUp: Boolean = false) {
    val scale = if (animate) {
        val transition = rememberInfiniteTransition(label = "flame")
        val value by transition.animateFloat(if (levelUp) 0.86f else 0.94f, if (levelUp) 1.14f else 1.04f, infiniteRepeatable(tween(if (levelUp) 800 else 1200), RepeatMode.Reverse), label = "flicker")
        value
    } else 1f
    Canvas(Modifier.size(100.dp)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * .5f, h * (1f - scale) * .2f)
            cubicTo(w * .35f, h * .28f, w * .82f, h * .32f, w * .78f, h * .63f)
            cubicTo(w * .76f, h * .9f, w * .65f, h, w * .5f, h)
            cubicTo(w * .25f, h, w * .12f, h * .82f, w * .24f, h * .57f)
            cubicTo(w * .31f, h * .43f, w * .37f, h * .35f, w * .5f, h * (1f - scale) * .2f)
            close()
        }
        drawPath(path, color)
        val inner = Path().apply {
            moveTo(w * .5f, h * .49f)
            cubicTo(w * .31f, h * .73f, w * .35f, h * .94f, w * .5f, h * .94f)
            cubicTo(w * .66f, h * .94f, w * .7f, h * .75f, w * .5f, h * .49f)
            close()
        }
        drawPath(inner, Color.White.copy(alpha = .55f))
    }
}

@Composable
internal fun streakMotionEnabled(): Boolean {
    val context = LocalContext.current
    return android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
}
