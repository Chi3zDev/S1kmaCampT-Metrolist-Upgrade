package com.metrolist.android.recap

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.metrolist.shared.db.MusicDatabase
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
import com.metrolist.shared.ui.icons.MetrolistIcons
import java.time.Instant
import java.time.ZoneId
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
    LaunchedEffect(database, period) {
        data = null
        failed = false
        try {
            data = database.loadRecap(period, Instant.now(), ZoneId.systemDefault())
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
            else -> RecapStory(recap, onBack, { showPeriods = true })
        }
    }
}

@Composable
private fun RecapStory(data: RecapData, onBack: () -> Unit, onPeriod: () -> Unit) {
    val pages = listOf(0, 1, 2)
    var page by remember(data) { mutableIntStateOf(0) }
    var progress by remember(data, page) { mutableFloatStateOf(0f) }
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(page, pressed) {
        while (!pressed && progress < 1f) {
            delay(50)
            progress = (progress + 1f / 120f).coerceAtMost(1f)
        }
        if (!pressed && progress >= 1f && page < pages.lastIndex) page++
    }
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxWidth().aspectRatio(9f / 16f)
            .background(colors.primaryContainer)
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
        Column(Modifier.align(Alignment.Center).padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (pages[page]) {
                0 -> { Text(data.songCount.toString(), style = MaterialTheme.typography.displayLarge); Text(stringResource(Res.string.recap_songs_count, data.songCount)) }
                1 -> { Text(data.minutes.toString(), style = MaterialTheme.typography.displayLarge); Text(stringResource(Res.string.recap_minutes)) }
                else -> { Text(stringResource(Res.string.recap_top_songs), style = MaterialTheme.typography.headlineLarge); data.songs.take(5).forEach { Text(it.title) } }
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
                TextButton(onClick = onPeriod) { Text(stringResource(Res.string.recap_title), color = colors.onPrimaryContainer) }
            }
        }
    }
}
