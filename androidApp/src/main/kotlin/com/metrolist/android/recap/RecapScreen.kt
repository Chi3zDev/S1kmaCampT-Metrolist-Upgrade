package com.metrolist.android.recap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.metrolist.android.utils.shareImage
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.LocalPlayerEffectsAllowed
import com.metrolist.shared.ui.PolishMotion
import com.metrolist.shared.ui.navigation.LocalRootContentActive
import com.metrolist.shared.ui.navigation.LocalContentObscured
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.back
import com.metrolist.shared.generated.resources.recap_empty
import com.metrolist.shared.generated.resources.recap_error
import com.metrolist.shared.generated.resources.recap_minutes
import com.metrolist.shared.generated.resources.recap_songs_count
import com.metrolist.shared.generated.resources.recap_month_filter
import com.metrolist.shared.generated.resources.recap_thirty_filter
import com.metrolist.shared.generated.resources.recap_title
import com.metrolist.shared.generated.resources.recap_top_artists
import com.metrolist.shared.generated.resources.recap_top_songs
import com.metrolist.shared.generated.resources.recap_year_filter
import com.metrolist.shared.generated.resources.share
import com.metrolist.shared.generated.resources.statistics_share_failed
import com.metrolist.shared.generated.resources.story_peak_day
import com.metrolist.shared.generated.resources.story_favorite_hour
import com.metrolist.shared.generated.resources.story_intro
import com.metrolist.shared.generated.resources.story_plays
import com.metrolist.shared.generated.resources.story_repeat
import com.metrolist.shared.generated.resources.story_summary
import com.metrolist.shared.generated.resources.story_change_period
import com.metrolist.shared.generated.resources.story_previous_month
import com.metrolist.shared.generated.resources.story_next_month
import com.metrolist.shared.ui.icons.MetrolistIcons
import java.time.LocalDate
import java.time.format.FormatStyle
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun RecapScreen(onBack: () -> Unit) {
    val database: MusicDatabase = koinInject()
    var period by remember { mutableStateOf(RecapPeriod.YEAR) }
    var data by remember { mutableStateOf<RecapData?>(null) }
    var failed by remember { mutableStateOf(false) }
    var showPeriods by remember { mutableStateOf(false) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    LaunchedEffect(database, period, month) {
        data = null
        failed = false
        try {
            data = database.loadRecap(period, Instant.now(), ZoneId.systemDefault(), month.takeIf { period == RecapPeriod.MONTH })
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            failed = true
        }
    }
    if (showPeriods) {
        AlertDialog(
            onDismissRequest = { showPeriods = false },
            title = { Text(stringResource(Res.string.recap_title)) },
            text = {
                Column {
                    listOf(RecapPeriod.YEAR, RecapPeriod.MONTH, RecapPeriod.LAST_30_DAYS).forEach { choice ->
                        val label = when (choice) {
                            RecapPeriod.YEAR -> Res.string.recap_year_filter
                            RecapPeriod.MONTH -> Res.string.recap_month_filter
                            else -> Res.string.recap_thirty_filter
                        }
                        TextButton(onClick = { period = choice; showPeriods = false }) { Text(stringResource(label)) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { month = month.minusMonths(1); period = RecapPeriod.MONTH }) {
                            Text(stringResource(Res.string.story_previous_month))
                        }
                        Text(month.format(DateTimeFormatter.ofPattern("MMM yyyy")))
                        TextButton(onClick = { month = month.plusMonths(1); period = RecapPeriod.MONTH }, enabled = month < YearMonth.now()) {
                            Text(stringResource(Res.string.story_next_month))
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
        val recap = data
        when {
            failed -> Text(stringResource(Res.string.recap_error))
            recap == null -> CircularProgressIndicator()
            recap.songs.isEmpty() -> Column {
                Text(stringResource(Res.string.recap_empty))
                TextButton(onClick = { showPeriods = true }) { Text(stringResource(Res.string.recap_title)) }
                TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) }
            }
            else -> RecapStory(recap, onBack, { showPeriods = true }, showPeriods)
        }
    }
}

@Composable
private fun RecapStory(data: RecapData, onBack: () -> Unit, onPeriod: () -> Unit, paused: Boolean) {
    val pages = buildList { add(0); add(1); if (data.artists.isNotEmpty()) add(2); add(3); if (data.songs.any { it.playCount > 1 }) add(4); if (data.peakDay != null) add(6); if (data.favoriteHour != null) add(7); add(5) }
    var page by remember(data) { mutableIntStateOf(0) }
    var progress by remember(data, page) { mutableFloatStateOf(0f) }
    var pressed by remember { mutableStateOf(false) }
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val visible = lifecycle == Lifecycle.State.RESUMED && LocalRootContentActive.current && !LocalContentObscured.current
    val motionScale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
    val motionEnabled = visible && motionScale > 0f && !Settings.shared.glassLightMode && LocalPlayerEffectsAllowed.current
    LaunchedEffect(page, pressed, paused, visible) {
        while (recapTimerRunning(visible, pressed, paused) && progress < 1f) {
            delay(50)
            progress = (progress + 1f / 120f).coerceAtMost(1f)
        }
        if (recapTimerRunning(visible, pressed, paused) && progress >= 1f && page < pages.lastIndex) page++
    }
    val colors = MaterialTheme.colorScheme
    val slideColor = when (pages[page]) {
        0 -> colors.primaryContainer
        1 -> colors.tertiaryContainer
        2 -> colors.secondaryContainer
        3 -> colors.primaryContainer
        4 -> colors.tertiaryContainer
        else -> colors.secondaryContainer
    }
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val shareLabel = stringResource(Res.string.share)
    val shareError = stringResource(Res.string.statistics_share_failed)
    var sharing by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxWidth().aspectRatio(9f / 16f)
            .pointerInput(page) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { offset: Offset ->
                        page = if (offset.x < size.width / 2) (page - 1).coerceAtLeast(0) else (page + 1).coerceAtMost(pages.lastIndex)
                        progress = 0f
                    },
                )
            }
            .pointerInput(Unit) {
                var drag = 0f
                detectVerticalDragGestures(onVerticalDrag = { _, amount -> drag += amount }, onDragEnd = {
                    if (drag > 100f) onBack()
                    drag = 0f
                })
            },
    ) {
        Box(
            Modifier.fillMaxSize().drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            }.background(Brush.linearGradient(listOf(colors.surface, slideColor, colors.surface))),
        ) {
        RecapBackdrop(pages[page], slideColor, colors.primary, colors.tertiary, motionEnabled)
        AnimatedContent(
            targetState = page,
            modifier = Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 88.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center,
            transitionSpec = {
                val duration = if (motionEnabled) PolishMotion.durationMillis else 0
                val direction = if (targetState >= initialState) 1 else -1
                val entrance = when (targetState % 3) {
                    0 -> fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { direction * it / 7 }
                    1 -> fadeIn(tween(duration)) + slideInVertically(tween(duration)) { it / 10 }
                    else -> fadeIn(tween(duration)) + scaleIn(tween(duration), initialScale = if (motionEnabled) .96f else 1f)
                }
                entrance.togetherWith(fadeOut(tween(duration)) + slideOutHorizontally(tween(duration)) { -direction * it / 12 })
            },
            label = "recapSlide",
        ) { slideIndex ->
            val slide = pages[slideIndex]
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (slide) {
                    0 -> {
                        CountingNumber(data.playCount.toLong(), motionEnabled)
                        Text(stringResource(Res.string.story_intro, data.playCount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    1 -> {
                        Text(stringResource(Res.string.recap_minutes), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        CountingNumber(data.minutes, motionEnabled)
                    }
                    2 -> {
                        Text(stringResource(Res.string.recap_top_artists), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        data.artists.take(5).forEachIndexed { index, artist ->
                            StoryRow(index + 1, artist.name, null, artist.thumbnailUrl, artist = true)
                        }
                    }
                    3 -> {
                        Text(stringResource(Res.string.recap_top_songs), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        data.songs.take(5).forEachIndexed { index, song -> StoryRow(index + 1, song.title, song.artists, song.thumbnailUrl) }
                    }
                    4 -> {
                        Text(stringResource(Res.string.story_repeat), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        data.songs.filter { it.playCount > 1 }.sortedByDescending { it.playCount }.take(5).forEachIndexed { index, song ->
                            StoryRow(index + 1, song.title, stringResource(Res.string.story_plays, song.playCount), song.thumbnailUrl)
                        }
                    }
                    6 -> data.peakDay?.let { bucket ->
                        Text(stringResource(Res.string.story_peak_day), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text(LocalDate.parse(bucket.bucket).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(Res.string.story_plays, bucket.playCount), style = MaterialTheme.typography.titleLarge)
                    }
                    7 -> data.favoriteHour?.let { bucket ->
                        Text(stringResource(Res.string.story_favorite_hour), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.getDefault(), "%02d:00", bucket.bucket.toInt()), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(Res.string.story_plays, bucket.playCount), style = MaterialTheme.typography.titleLarge)
                    }
                    else -> {
                        Text(stringResource(Res.string.story_summary), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(Res.string.story_plays, data.playCount), style = MaterialTheme.typography.titleLarge)
                        Text("${data.minutes} ${stringResource(Res.string.recap_minutes)}", style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(Res.string.recap_top_songs), style = MaterialTheme.typography.titleLarge)
                        data.songs.take(3).forEach { Text(it.title) }
                        if (data.artists.isNotEmpty()) Text(stringResource(Res.string.recap_top_artists), style = MaterialTheme.typography.titleLarge)
                        data.artists.take(3).forEach { Text(it.name) }
                    }
                }
            }
        }
        }
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                pages.forEachIndexed { index, _ ->
                    Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.onPrimaryContainer.copy(alpha = .25f))) {
                        Box(Modifier.fillMaxWidth(if (index < page) 1f else if (index == page) progress else 0f).height(4.dp).background(colors.onPrimaryContainer))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) { Icon(MetrolistIcons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back), tint = colors.onPrimaryContainer) }
                TextButton(onClick = onPeriod) { Text(stringResource(Res.string.story_change_period), color = colors.onPrimaryContainer) }
                IconButton(onClick = {
                    sharing = true
                    pressed = true
                    scope.launch {
                        try {
                            val captured = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            val bitmap = Bitmap.createScaledBitmap(captured, 1080, 1920, true).asImageBitmap()
                            shareImage(context, bitmap, shareLabel)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            Toast.makeText(context, shareError, Toast.LENGTH_SHORT).show()
                        } finally {
                            sharing = false
                            pressed = false
                        }
                    }
                }, enabled = !sharing) { Icon(MetrolistIcons.Outlined.Share, shareLabel, tint = colors.onPrimaryContainer) }
            }
        }
    }
}

@Composable
private fun CountingNumber(value: Long, animate: Boolean) {
    var started by remember(value) { mutableStateOf(!animate) }
    LaunchedEffect(value, animate) { started = true }
    val fraction by animateFloatAsState(if (started || !animate) 1f else 0f, animationSpec = tween(if (animate) PolishMotion.durationMillis else 0), label = "recapNumber")
    Text((value * fraction).toLong().toString(), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clearAndSetSemantics { contentDescription = value.toString() })
}

@Composable
private fun StoryRow(rank: Int, title: String, subtitle: String?, artwork: String?, artist: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(rank.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        AsyncImage(model = artwork, contentDescription = null, modifier = Modifier.size(48.dp).clip(if (artist) CircleShape else RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            if (!subtitle.isNullOrBlank()) Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecapBackdrop(slide: Int, base: androidx.compose.ui.graphics.Color, primary: androidx.compose.ui.graphics.Color, accent: androidx.compose.ui.graphics.Color, animate: Boolean) {
    val phaseState = if (animate) {
        val transition = rememberInfiniteTransition(label = "recapMesh")
        transition.animateFloat(0f, 1f, infiniteRepeatable(tween(18_000, easing = LinearEasing), RepeatMode.Reverse), label = "recapMeshPhase")
    } else null
    Canvas(Modifier.fillMaxSize()) {
        // Keep mesh animation updates in the draw phase instead of recomposing the screen.
        val phase = phaseState?.value ?: 0f
        val drift = (phase - .5f) * size.width * .12f
        val positions = when (slide % 3) {
            0 -> listOf(Offset(size.width * .85f + drift, size.height * .18f), Offset(size.width * .15f - drift, size.height * .78f))
            1 -> listOf(Offset(size.width * .15f - drift, size.height * .25f), Offset(size.width * .85f + drift, size.height * .68f))
            else -> listOf(Offset(size.width * .55f + drift, size.height * .10f), Offset(size.width * .35f - drift, size.height * .90f))
        }
        positions.forEachIndexed { i, center ->
            val color = if (i == 0) primary else accent
            val radius = size.width * if (i == 0) .75f else .60f
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = .32f), base.copy(alpha = 0f)), center, radius), radius, center)
        }
    }
}
