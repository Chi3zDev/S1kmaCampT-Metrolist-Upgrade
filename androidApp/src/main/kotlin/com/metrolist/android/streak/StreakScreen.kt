@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.metrolist.android.streak

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.metrolist.android.BuildConfig
import com.metrolist.android.LocalMiniPlayerBottomInset
import com.metrolist.android.playback.LocalPlayerConnection
import com.metrolist.android.ui.component.GlassScreenFrame
import com.metrolist.android.ui.component.tabContentTopPadding
import com.metrolist.android.utils.shareImage
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.*
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.LocalPlayerEffectsAllowed
import com.metrolist.shared.ui.PolishMotion
import com.metrolist.shared.ui.components.AlertDialog
import com.metrolist.shared.ui.components.ExpressiveSwitch
import com.metrolist.shared.ui.components.LiquidGlassButton
import com.metrolist.shared.ui.components.LiquidGlassSurface
import com.metrolist.shared.ui.components.LiquidGlassTextButton
import com.metrolist.shared.ui.components.appPageBackground
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.ui.components.glassEdgeBrush
import com.metrolist.shared.ui.components.liquidGlassBackdropReady
import com.metrolist.shared.ui.haptics.HapticEvent
import com.metrolist.shared.ui.haptics.rememberMetrolistHaptics
import com.metrolist.shared.ui.navigation.LocalContentObscured
import com.metrolist.shared.ui.navigation.LocalRootContentActive
import kotlinx.coroutines.CancellationException
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
    val context = LocalContext.current
    val reminderPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Settings.shared.streakReminderEnabled = granted
            StreakReminderWorker.schedule(context, granted, Settings.shared.streakReminderMinuteOfDay)
        }
    val timestamps by remember(
        database,
    ) { database.dao().qualifyingStreakTimestamps() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val screenActive = lifecycle == Lifecycle.State.RESUMED && LocalRootContentActive.current && !LocalContentObscured.current
    val playerConnection = LocalPlayerConnection.current
    var liveListeningMs by remember(playerConnection) { mutableStateOf<Long?>(null) }
    LaunchedEffect(screenActive, playerConnection) {
        while (screenActive) {
            liveListeningMs = playerConnection?.currentStreakListeningMs()
            delay(1000)
        }
        liveListeningMs = null
    }
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
    var showShare by remember { mutableStateOf(false) }
    var previewLevel by remember { mutableStateOf<StreakLevel?>(null) }

    if (showShare) {
        var shareBackground by remember { mutableIntStateOf(0) }
        var sharing by remember { mutableStateOf(false) }
        val levelLabel = stringResource(shownLevel.title)
        val daysLabel = stringResource(Res.string.streak_days)
        val weekLabel = stringResource(Res.string.streak_week)
        val longestLabel = stringResource(Res.string.streak_longest, streak.longest)
        val shareLabel = stringResource(Res.string.streak_share)
        val errorLabel = stringResource(Res.string.streak_share_error)
        val accent = flameColor.toArgb()
        val themeBackground = MaterialTheme.colorScheme.surface.toArgb()
        val themeForeground = MaterialTheme.colorScheme.onSurface.toArgb()
        val weekdays =
            (0..6).map { index ->
                val day = today.minusDays((today.dayOfWeek.value - 1).toLong()).plusDays(index.toLong())
                day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, locale) to (day in allDays)
            }
        val backgrounds =
            listOf(
                stringResource(Res.string.streak_share_level),
                stringResource(Res.string.streak_share_theme),
                stringResource(Res.string.streak_share_solid),
            )
        AlertDialog(
            onDismissRequest = { if (!sharing) showShare = false },
            title = { Text(shareLabel) },
            text = {
                Column {
                    Text(stringResource(Res.string.streak_share_background))
                    backgrounds.forEachIndexed { index, text ->
                        Row(Modifier.fillMaxWidth().clickable { shareBackground = index }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = shareBackground == index, onClick = { shareBackground = index })
                            Text(text)
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    listOf(false, true).forEach { story ->
                        LiquidGlassTextButton(enabled = !sharing, onClick = {
                            sharing = true
                            scope.launch {
                                try {
                                    val background =
                                        when (shareBackground) {
                                            0 -> {
                                                android.graphics.Color.rgb(
                                                    android.graphics.Color.red(accent) / 5,
                                                    android.graphics.Color.green(accent) / 5,
                                                    android.graphics.Color.blue(accent) / 5,
                                                )
                                            }

                                            1 -> {
                                                themeBackground
                                            }

                                            else -> {
                                                android.graphics.Color.rgb(20, 20, 24)
                                            }
                                        }
                                    val foreground = if (shareBackground == 1) themeForeground else android.graphics.Color.WHITE
                                    val image =
                                        withContext(Dispatchers.Default) {
                                            renderStreakShare(
                                                context,
                                                streak.current,
                                                longestLabel,
                                                levelLabel,
                                                daysLabel,
                                                weekLabel,
                                                weekdays,
                                                accent,
                                                shownLevel.flameVariant,
                                                extinguished,
                                                background,
                                                foreground,
                                                story,
                                            )
                                        }
                                    shareImage(context, image, shareLabel)
                                    showShare = false
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Exception) {
                                    Toast.makeText(context, errorLabel, Toast.LENGTH_SHORT).show()
                                } finally {
                                    sharing = false
                                }
                            }
                        }) { Text(stringResource(if (story) Res.string.streak_share_story else Res.string.streak_share_square)) }
                    }
                }
            },
            dismissButton = {
                LiquidGlassTextButton(
                    onClick = { showShare = false },
                    enabled = !sharing,
                ) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }

    val backgroundEnabled = Settings.shared.streakLevelBackgroundEnabled
    val pageSurface = appPageBackground(MaterialTheme.colorScheme.surface)
    GlassScreenFrame(
        topInset = tabContentTopPadding(),
        barHeight = 64.dp,
        topBar = { StreakTopBar(onBack) },
    ) { viewport ->
        LazyColumn(
            modifier =
                viewport.background(
                    if (backgroundEnabled) {
                        Brush.verticalGradient(listOf(flameColor.copy(alpha = 0.15f), pageSurface, pageSurface))
                    } else {
                        Brush.verticalGradient(listOf(pageSurface, pageSurface))
                    },
                ),
            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom =
                        LocalMiniPlayerBottomInset.current + 32.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            item {
                if (liquidGlassBackdropReady()) {
                    LiquidGlassButton(onClick = { showShare = true }) { Text(stringResource(Res.string.streak_share)) }
                } else {
                    OutlinedButton(onClick = { showShare = true }) { Text(stringResource(Res.string.streak_share)) }
                }
            }
            item {
                val label = stringResource(Res.string.streak_level_background)
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    ExpressiveSwitch(
                        checked = backgroundEnabled,
                        onCheckedChange = { Settings.shared.streakLevelBackgroundEnabled = it },
                        modifier = Modifier.semantics { contentDescription = label },
                    )
                }
            }
            item {
                val reminderLabel = stringResource(Res.string.streak_reminder_title)
                val enabled = Settings.shared.streakReminderEnabled
                val minuteOfDay = Settings.shared.streakReminderMinuteOfDay.coerceIn(0, 1439)
                StreakPanel {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(reminderLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        ExpressiveSwitch(
                            checked = enabled,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= 33) {
                                    reminderPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    Settings.shared.streakReminderEnabled = checked
                                    StreakReminderWorker.schedule(context, checked, minuteOfDay)
                                }
                            },
                            modifier = Modifier.semantics { contentDescription = reminderLabel },
                        )
                    }
                    Text(stringResource(Res.string.streak_reminder_timing), style = MaterialTheme.typography.bodySmall)
                    LiquidGlassTextButton(onClick = {
                        TimePickerDialog(context, { _, hour, minute ->
                            val value = hour * 60 + minute
                            Settings.shared.streakReminderMinuteOfDay = value
                            if (Settings.shared.streakReminderEnabled) StreakReminderWorker.schedule(context, true, value)
                        }, minuteOfDay / 60, minuteOfDay % 60, true).show()
                    }) {
                        Text(String.format(locale, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60))
                    }
                }
            }
            if (BuildConfig.DEBUG && showDebug) {
                item {
                    StreakPanel {
                        Text(stringResource(Res.string.streak_debug_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(Res.string.streak_debug_date, today.toString(), debugDays))
                        Text(stringResource(Res.string.streak_debug_read_only), style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LiquidGlassTextButton(onClick = { debugDays = (debugDays - 1).coerceAtLeast(-366) }) { Text("−1") }
                            LiquidGlassTextButton(onClick = { debugDays = 0 }) { Text(stringResource(Res.string.streak_debug_reset)) }
                            LiquidGlassTextButton(onClick = { debugDays = (debugDays + 1).coerceAtMost(366) }) { Text("+1") }
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
                        val motionEnabled = screenActive && streakMotionEnabled()
                        val minimumMs = (Settings.shared.historyDuration * 1000f).toLong().coerceAtLeast(30_000L)
                        val todayProgress = streakTodayProgress(streak.todayCounted, liveListeningMs.takeUnless { simulated }, minimumMs)
                        val ringProgress by animateFloatAsState(
                            todayProgress,
                            animationSpec = tween(if (motionEnabled) PolishMotion.durationMillis else 0),
                            label = "streakTodayProgress",
                        )
                        InteractiveStreakFlame(
                            color = flameColor,
                            variant = shownLevel.flameVariant,
                            extinguished = extinguished,
                            animate = motionEnabled,
                            progress = ringProgress,
                            counted = streak.todayCounted,
                        )
                        StreakTodayBadge(streak.todayCounted, flameColor, motionEnabled)
                        StreakCount(streak.current, screenActive && streakMotionEnabled(), MaterialTheme.typography.displayMedium)
                        LiquidGlassTextButton(onClick = { showLevels = true }) {
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
                            if (liquidGlassBackdropReady()) {
                                LiquidGlassButton(onClick = { confirmation = true }, enabled = !simulated) {
                                    Text(stringResource(Res.string.streak_restore))
                                }
                            } else {
                                Button(onClick = { confirmation = true }, enabled = !simulated) {
                                    Text(stringResource(Res.string.streak_restore))
                                }
                            }
                        }
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
                        variant = level.flameVariant,
                    )
                    Text(stringResource(Res.string.streak_level_requirement, level.minimumDays))
                    if (!unlocked) Text(stringResource(Res.string.streak_level_locked))
                }
            },
            confirmButton = {
                LiquidGlassTextButton(onClick = {
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
                LiquidGlassTextButton(onClick = {
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
            dismissButton = { LiquidGlassTextButton(onClick = { confirmation = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun InteractiveStreakFlame(
    color: Color,
    variant: Int,
    extinguished: Boolean,
    animate: Boolean,
    progress: Float,
    counted: Boolean,
) {
    val scope = rememberCoroutineScope()
    val haptics = rememberMetrolistHaptics()
    val burst = remember { Animatable(0f) }
    var touch by remember { mutableStateOf(Offset.Zero) }
    val label = stringResource(Res.string.streak_flame_interaction)

    fun trigger(offset: Offset) {
        haptics.perform(HapticEvent.PRESS)
        if (animate && !extinguished) {
            touch = offset
            scope.launch {
                burst.snapTo(1f)
                burst.animateTo(0f, PolishMotion.interactiveSpring())
            }
        }
    }
    Box(
        Modifier
            .size(132.dp)
            .pointerInput(animate, extinguished) { detectTapGestures { trigger(it) } }
            .semantics {
                contentDescription = label
                onClick {
                    trigger(Offset.Zero)
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = color.copy(alpha = 0.18f),
            strokeWidth = 6.dp,
        )
        Flame(
            color,
            animate = animate,
            particles = !extinguished,
            extinguished = extinguished,
            variant = variant,
            modifier =
                Modifier.graphicsLayer {
                    val scale = 1f + burst.value * 0.15f
                    scaleX = scale
                    scaleY = scale
                },
        )
        Canvas(Modifier.fillMaxSize()) {
            val strength = burst.value
            if (strength > 0f) {
                val center = if (touch == Offset.Zero) this.center else touch
                drawCircle(color.copy(alpha = 0.25f * strength), radius = size.minDimension * 0.30f, center = center)
                repeat(8) { i ->
                    val angle = i * kotlin.math.PI.toFloat() / 4f
                    val distance = size.minDimension * (0.06f + 0.25f * (1f - strength))
                    drawCircle(
                        color.copy(alpha = strength),
                        radius = size.minDimension * 0.018f,
                        center = center + Offset(kotlin.math.cos(angle) * distance, kotlin.math.sin(angle) * distance),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = counted,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter =
                fadeIn(tween(if (animate) 300 else 0)) + scaleIn(tween(if (animate) 300 else 0), initialScale = if (animate) 0.6f else 1f),
        ) { Text("✓", color = color, style = MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun StreakTodayBadge(
    counted: Boolean,
    color: Color,
    animate: Boolean,
) {
    val pulse =
        if (!counted && animate) {
            val transition = rememberInfiniteTransition(label = "streakTodayPulse")
            transition
                .animateFloat(
                    0.18f,
                    0.46f,
                    infiniteRepeatable(tween(1500), RepeatMode.Reverse),
                    label = "streakTodayPulseAlpha",
                ).value
        } else {
            0.18f
        }
    Text(
        stringResource(Res.string.streak_day_today),
        modifier =
            Modifier
                .border(1.dp, color.copy(alpha = pulse), RoundedCornerShape(50))
                .background(color.copy(alpha = 0.08f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StreakTopBar(onBack: () -> Unit) {
    LiquidGlassSurface(radius = 24.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidGlassTextButton(onClick = onBack) { Text("‹  " + stringResource(Res.string.back)) }
                Text(stringResource(Res.string.streak_page_title), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

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
                                variant = level.flameVariant,
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
        confirmButton = { LiquidGlassTextButton(onClick = onDismiss) { Text(stringResource(Res.string.back)) } },
    )
}

@Composable
private fun StreakPanel(content: @Composable ColumnScope.() -> Unit) {
    LiquidGlassSurface(radius = 24.dp, modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh)),
            border = glassCardBorder(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
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
    val glass = liquidGlassBackdropReady()
    val background =
        when {
            day in listened -> c.primary
            day in restored -> c.tertiary
            day == today -> c.secondaryContainer
            else -> c.surfaceVariant
        }.let { if (glass) it.copy(alpha = if (day in listened || day in restored) 0.40f else 0.24f) else it }
    val foreground =
        when {
            glass -> c.onSurface
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
            .then(
                when {
                    day == today -> Modifier.border(2.dp, c.outline, CircleShape)
                    glass -> Modifier.border(1.dp, glassEdgeBrush(Settings.shared.glassStyle == "CLEAR"), CircleShape)
                    else -> Modifier
                },
            ),
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
    variant: Int = 0,
) {
    StreakFlame(
        level = variant + 1,
        size = 100.dp,
        state = StreakFlameState(animate = animate, extinguished = extinguished, particles = particles),
        modifier = modifier,
    )
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
