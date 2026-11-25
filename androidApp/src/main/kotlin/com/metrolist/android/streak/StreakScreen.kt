@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.metrolist.android.streak

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.metrolist.android.BuildConfig
import com.metrolist.android.LocalMiniPlayerBottomInset
import com.metrolist.android.ui.component.tabContentTopPadding
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.*
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.LocalPlayerEffectsAllowed
import com.metrolist.shared.ui.PolishMotion
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.ui.navigation.LocalContentObscured
import com.metrolist.shared.ui.navigation.LocalRootContentActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

@Composable
fun StreakScreen(onBack: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val database: MusicDatabase = koinInject()
    val timestamps by remember(
        database,
    ) { database.dao().qualifyingStreakTimestamps() }.collectAsStateWithLifecycle(initialValue = emptyList())
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
    var debugTaps by remember { mutableIntStateOf(0) }
    var showDebug by remember { mutableStateOf(false) }
    var debugDays by remember { mutableIntStateOf(0) }
    // The simulator only changes what this page displays. It never writes listening or recovery data.
    val effectiveNow = if (BuildConfig.DEBUG) now.atZone(zone).plusDays(debugDays.toLong()).toInstant() else now
    val simulated = BuildConfig.DEBUG && debugDays != 0
    val today = effectiveNow.atZone(zone).toLocalDate()
    val recoveryJson = Settings.shared.streakRecoveryJson
    val ledger = remember(recoveryJson) { decodeStreakRecovery(recoveryJson) }
    val restored = ledger?.recoveredDays.orEmpty()
    val listened =
        remember(timestamps, zone, today) {
            timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.filter { !it.isAfter(today) }.toSet()
        }
    val streak = remember(timestamps, effectiveNow, zone, restored) { calculateListeningStreak(timestamps, effectiveNow, zone, restored) }
    val plan = remember(timestamps, effectiveNow, zone, ledger) { ledger?.let { planStreakRecovery(timestamps, effectiveNow, zone, it) } }
    val allDays = (listened + restored).filter { !it.isAfter(today) }.toSet()
    val latest = allDays.maxOrNull()
    val extinguished = latest != null && latest.isBefore(today.minusDays(1))
    val previousLength =
        remember(timestamps, effectiveNow, zone, restored) { lastListeningStreak(timestamps, effectiveNow, zone, restored) }
    val shownLevel = streakLevel(if (extinguished) previousLength else streak.current)
    val targetFlameColor = if (extinguished) MaterialTheme.colorScheme.outline else shownLevel.color
    val flameColor =
        if (screenActive && streakMotionEnabled()) {
            animateColorAsState(targetFlameColor, animationSpec = tween(PolishMotion.durationMillis), label = "streakFlameColor").value
        } else {
            targetFlameColor
        }
    val next = if (extinguished) null else streakLevels.firstOrNull { it.minimumDays > streak.current }
    val progress =
        if (extinguished) {
            0f
        } else if (next ==
            null
        ) {
            1f
        } else {
            ((streak.current - shownLevel.minimumDays).toFloat() / (next.minimumDays - shownLevel.minimumDays)).coerceIn(0f, 1f)
        }
    val scope = rememberCoroutineScope()
    var confirmation by remember { mutableStateOf(false) }
    var restoreFailed by remember { mutableStateOf(false) }
    var showLevels by remember { mutableStateOf(false) }
    var previewLevel by remember { mutableStateOf<StreakLevel?>(null) }

    LazyColumn(
        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = tabContentTopPadding(),
                bottom =
                    LocalMiniPlayerBottomInset.current + 32.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TextButton(onClick = onBack) { Text("‹  " + stringResource(Res.string.back)) } }
        item {
            Text(
                stringResource(Res.string.streak_page_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier =
                    if (BuildConfig.DEBUG) {
                        Modifier.clickable {
                            debugTaps++
                            if (debugTaps >= 7) {
                                showDebug = !showDebug
                                if (!showDebug) debugDays = 0
                                debugTaps = 0
                            }
                        }
                    } else {
                        Modifier
                    },
            )
        }
        if (BuildConfig.DEBUG && showDebug) {
            item {
                StreakPanel {
                    Text(stringResource(Res.string.streak_debug_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(Res.string.streak_debug_date, today.toString(), debugDays))
                    Text(stringResource(Res.string.streak_debug_read_only), style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { debugDays = (debugDays - 1).coerceAtLeast(-366) }) { Text("−1") }
                        TextButton(onClick = { debugDays = 0 }) { Text(stringResource(Res.string.streak_debug_reset)) }
                        TextButton(onClick = { debugDays = (debugDays + 1).coerceAtMost(366) }) { Text("+1") }
                    }
                }
            }
        }
        item {
            StreakPanel {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Flame(
                        flameColor,
                        animate = screenActive && streakMotionEnabled(),
                        particles = !extinguished,
                        extinguished = extinguished,
                    )
                    StreakCount(streak.current, screenActive && streakMotionEnabled(), MaterialTheme.typography.displayMedium)
                    TextButton(onClick = { showLevels = true }) {
                        Text(stringResource(shownLevel.title), color = flameColor, style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        stringResource(
                            when {
                                extinguished -> Res.string.streak_extinguished
                                streak.todayCounted -> Res.string.streak_active
                                else -> Res.string.streak_waiting
                            },
                        ),
                        textAlign = TextAlign.Center,
                    )
                    if (extinguished) Text(stringResource(Res.string.streak_previous, previousLength))
                    streakRemainingMinutes(streak, effectiveNow, zone)?.let { remaining ->
                        Text(
                            stringResource(Res.string.streak_expires_in, remaining / 60, remaining % 60),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = flameColor)
                    if (next !=
                        null
                    ) {
                        Text(
                            stringResource(Res.string.streak_next, stringResource(next.title)),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
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
                            Text(
                                day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                                style = MaterialTheme.typography.labelSmall,
                            )
                            StaggeredDayDot(day, today, listened, restored, offset, screenActive && streakMotionEnabled())
                        }
                    }
                }
            }
        }
        item {
            StreakPanel {
                val month = YearMonth.from(today)
                Text(stringResource(Res.string.streak_calendar), style = MaterialTheme.typography.titleMedium)
                Text(
                    "${month.month.getDisplayName(TextStyle.FULL, locale)} ${month.year}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                val blanks = month.atDay(1).dayOfWeek.value - 1
                val rows = (blanks + month.lengthOfMonth() + 6) / 7
                repeat(rows) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { col ->
                            val day = row * 7 + col - blanks + 1
                            if (day in 1..month.lengthOfMonth()) {
                                DayDot(month.atDay(day), today, listened, restored)
                            } else {
                                Spacer(Modifier.size(36.dp))
                            }
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
                if (ledger == null) {
                    Text(stringResource(Res.string.streak_restore_corrupt), color = MaterialTheme.colorScheme.error)
                } else {
                    val remaining = ledger.remaining(today)
                    Text(stringResource(Res.string.streak_recovery_remaining, remaining, MONTHLY_STREAK_RECOVERIES))
                    Text(
                        "●".repeat(remaining) + "○".repeat(MONTHLY_STREAK_RECOVERIES - remaining),
                        color = flameColor,
                        modifier = Modifier.clearAndSetSemantics { },
                    )
                    val nextReset = YearMonth.from(today).plusMonths(1).atDay(1)
                    Text(
                        stringResource(
                            Res.string.streak_recovery_reset,
                            nextReset.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    when (plan?.failure) {
                        StreakRecoveryFailure.GAP_TOO_LARGE -> Text(stringResource(Res.string.streak_restore_too_long))
                        StreakRecoveryFailure.INSUFFICIENT_QUOTA -> Text(stringResource(Res.string.streak_restore_no_quota))
                        else -> Unit
                    }
                    if (plan?.canRecover ==
                        true
                    ) {
                        Button(
                            onClick = { confirmation = true },
                            enabled = !simulated,
                        ) { Text(stringResource(Res.string.streak_restore)) }
                    }
                }
            }
        }
    }
    if (showLevels) {
        StreakLevelGallery(
            longest = streak.longest,
            onDismiss = { showLevels = false },
            onSelect = { level ->
                previewLevel = level
                showLevels = false
            },
        )
    }
    previewLevel?.let { level ->
        val unlocked = streak.longest >= level.minimumDays
        AlertDialog(
            onDismissRequest = { previewLevel = null },
            title = { Text(stringResource(level.title)) },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Flame(
                        color = if (unlocked) level.color else MaterialTheme.colorScheme.outline,
                        animate = screenActive && streakMotionEnabled(),
                        particles = unlocked,
                        extinguished = !unlocked,
                        modifier = Modifier.size(140.dp),
                    )
                    Text(stringResource(Res.string.streak_level_requirement, level.minimumDays))
                    if (!unlocked) Text(stringResource(Res.string.streak_level_locked))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    previewLevel = null
                    showLevels = true
                }) { Text(stringResource(Res.string.back)) }
            },
        )
    }
    if (confirmation && plan != null && !simulated) {
        AlertDialog(
            onDismissRequest = { confirmation = false },
            title = { Text(stringResource(Res.string.streak_restore)) },
            text = {
                Text(
                    stringResource(Res.string.streak_restore_confirm, plan.missingDays.size, plan.remaining - plan.missingDays.size),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmation = false
                    scope.launch {
                        restoreFailed =
                            !withContext(Dispatchers.IO) {
                                runCatching {
                                    val freshTimestamps = database.dao().qualifyingStreakTimestamps().first()
                                    persistStreakRecovery(freshTimestamps, Instant.now(), ZoneId.systemDefault())
                                }.getOrDefault(false)
                            }
                    }
                }) { Text(stringResource(Res.string.streak_restore)) }
            },
            dismissButton = { TextButton(onClick = { confirmation = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StreakLevelGallery(
    longest: Int,
    onDismiss: () -> Unit,
    onSelect: (StreakLevel) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.streak_levels_title)) },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                streakLevels.forEach { level ->
                    val unlocked = longest >= level.minimumDays
                    Surface(
                        onClick = { onSelect(level) },
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Flame(
                                color = if (unlocked) level.color else MaterialTheme.colorScheme.outline,
                                animate = false,
                                extinguished = !unlocked,
                                modifier = Modifier.size(54.dp),
                            )
                            Text(stringResource(level.title), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                            Text(
                                stringResource(Res.string.streak_level_requirement, level.minimumDays),
                                style = MaterialTheme.typography.labelSmall,
                            )
                            if (!unlocked) Text(stringResource(Res.string.streak_level_locked), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.back)) } },
    )
}

@Composable
private fun StreakPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
        border = glassCardBorder(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
internal fun DayDot(
    day: LocalDate,
    today: LocalDate,
    listened: Set<LocalDate>,
    restored: Set<LocalDate>,
) {
    val c = MaterialTheme.colorScheme
    val background =
        when {
            day in listened -> c.primary
            day in restored -> c.tertiary
            day == today -> c.secondaryContainer
            else -> c.surfaceVariant
        }
    val foreground =
        when {
            day in listened -> c.onPrimary
            day in restored -> c.onTertiary
            else -> c.onSurfaceVariant
        }
    val status =
        stringResource(
            when {
                day in listened -> Res.string.streak_day_listened
                day in restored -> Res.string.streak_day_restored
                else -> Res.string.streak_day_empty
            },
        )
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(day, locale) { day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) }
    val todayLabel = if (day == today) stringResource(Res.string.streak_day_today) else null
    val description = listOfNotNull(date, status, todayLabel).joinToString(", ")
    Box(
        Modifier
            .size(36.dp)
            .padding(2.dp)
            .clearAndSetSemantics { contentDescription = description }
            .background(background, CircleShape)
            .then(if (day == today) Modifier.border(2.dp, c.outline, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(day.dayOfMonth.toString(), color = foreground, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun Flame(
    color: Color,
    animate: Boolean,
    levelUp: Boolean = false,
    particles: Boolean = false,
    extinguished: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var entered by remember { mutableStateOf(!animate) }
    LaunchedEffect(animate, extinguished) { entered = true }
    val targetScale =
        if (extinguished) {
            .82f
        } else if (!entered && animate) {
            .72f
        } else {
            1f
        }
    val entranceScale by animateFloatAsState(
        targetScale,
        animationSpec =
            if (animate &&
                levelUp
            ) {
                PolishMotion.interactiveSpring()
            } else {
                tween(if (animate) PolishMotion.durationMillis else 0)
            },
        label = "flameEntrance",
    )
    val phaseState =
        if (animate) {
            val transition = rememberInfiniteTransition(label = "flame")
            transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "flamePhase")
        } else {
            null
        }
    Canvas(
        modifier.size(100.dp).graphicsLayer {
            scaleX = entranceScale
            scaleY = entranceScale
        },
    ) {
        // Read the changing phase in draw, not composition, so only this Canvas is invalidated.
        val phase = phaseState?.value ?: 0f
        val flicker =
            if (animate &&
                !extinguished
            ) {
                kotlin.math.sin(phase * 6.283185f) * 0.035f + kotlin.math.sin(phase * 18.849556f) * 0.015f
            } else {
                0f
            }
        val scale = 1f + flicker
        val w = size.width
        val h = size.height
        val glow = if (animate && !extinguished) (0.10f + flicker).coerceIn(0.04f, 0.16f) else 0.04f
        drawCircle(
            Brush.radialGradient(
                listOf(color.copy(alpha = glow), Color.Transparent),
                center = Offset(w * .5f, h * .62f),
                radius =
                    w * .48f,
            ),
            radius = w * .48f,
            center = Offset(w * .5f, h * .62f),
        )
        if (animate && (particles || extinguished)) {
            repeat(4) { index ->
                val step = (phase + index * .25f) % 1f
                val alpha = (1f - step) * if (extinguished) .22f else .70f
                val x = w * (.30f + index * .13f) + kotlin.math.sin((phase + index) * 6.283185f) * w * .025f
                val y = h * (.72f - step * .68f)
                drawCircle(
                    color.copy(alpha = alpha),
                    radius = w * (if (extinguished) .025f + step * .025f else .016f),
                    center = Offset(x, y),
                )
            }
        }
        val path =
            Path().apply {
                moveTo(w * .5f, h * (1f - scale) * .2f)
                cubicTo(w * .35f, h * .28f, w * .82f, h * .32f, w * .78f, h * .63f)
                cubicTo(w * .76f, h * .9f, w * .65f, h, w * .5f, h)
                cubicTo(w * .25f, h, w * .12f, h * .82f, w * .24f, h * .57f)
                cubicTo(w * .31f, h * .43f, w * .37f, h * .35f, w * .5f, h * (1f - scale) * .2f)
                close()
            }
        drawPath(path, color.copy(alpha = if (extinguished) .55f else 1f))
        val inner =
            Path().apply {
                moveTo(w * .5f, h * .49f)
                cubicTo(w * .31f, h * .73f, w * .35f, h * .94f, w * .5f, h * .94f)
                cubicTo(w * .66f, h * .94f, w * .7f, h * .75f, w * .5f, h * .49f)
                close()
            }
        drawPath(inner, Color.White.copy(alpha = if (extinguished) .12f else .55f))
    }
}

@Composable
internal fun streakMotionEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val systemAllowsMotion =
        remember(context, lifecycle) {
            android.provider.Settings.Global
                .getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) >
                0f
        }
    return streakMotionAllowed(systemAllowsMotion, Settings.shared.glassLightMode, LocalPlayerEffectsAllowed.current)
}

@Composable
internal fun StreakCount(
    count: Int,
    animate: Boolean,
    style: androidx.compose.ui.text.TextStyle,
) {
    var target by remember { mutableIntStateOf(if (animate) 0 else count) }
    LaunchedEffect(count, animate) { target = count }
    val value by animateIntAsState(target, animationSpec = tween(if (animate) PolishMotion.durationMillis else 0), label = "streakCount")
    Text(
        if (animate) value.toString() else count.toString(),
        style = style,
        modifier = Modifier.clearAndSetSemantics { contentDescription = count.toString() },
    )
}

@Composable
internal fun StaggeredDayDot(
    day: LocalDate,
    today: LocalDate,
    listened: Set<LocalDate>,
    restored: Set<LocalDate>,
    index: Int,
    animate: Boolean,
) {
    var shown by remember(day) { mutableStateOf(!animate) }
    LaunchedEffect(day, animate) {
        if (animate) {
            shown = false
            delay(index * PolishMotion.staggerMillis)
        }
        shown = true
    }
    val alpha by animateFloatAsState(
        if (!animate ||
            shown
        ) {
            1f
        } else {
            0f
        },
        tween(if (animate) PolishMotion.durationMillis else 0),
        label = "streakDay",
    )
    Box(Modifier.graphicsLayer { this.alpha = alpha }) { DayDot(day, today, listened, restored) }
}
