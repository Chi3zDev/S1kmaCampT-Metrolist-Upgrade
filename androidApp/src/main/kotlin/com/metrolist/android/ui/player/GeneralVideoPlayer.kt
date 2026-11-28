package com.metrolist.android.ui.player

import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.android.extensions.findActivity
import com.metrolist.android.playback.PlayerConnection
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.back
import com.metrolist.shared.generated.resources.enter_fullscreen
import com.metrolist.shared.generated.resources.experimental_video_live
import com.metrolist.shared.generated.resources.music_video_quality
import com.metrolist.shared.generated.resources.music_video_quality_auto
import com.metrolist.shared.generated.resources.music_video_quality_resolution
import com.metrolist.shared.generated.resources.next
import com.metrolist.shared.generated.resources.pause
import com.metrolist.shared.generated.resources.play
import com.metrolist.shared.generated.resources.previous
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.isLive
import com.metrolist.shared.settings.MusicVideoQuality
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.utils.formatDuration
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/** Video-specific presentation using the existing Media3 player. */
@Composable
fun GeneralVideoPlayer(
    song: SongItem,
    connection: PlayerConnection,
    player: Player?,
    hasVideo: Boolean,
    onClose: () -> Unit,
) {
    var fullscreen by rememberSaveable(song.id) { mutableStateOf(false) }
    var showControls by remember(song.id) { mutableStateOf(true) }
    var qualityMenu by remember { mutableStateOf(false) }
    var interactionCount by remember { mutableStateOf(0) }
    val activity = LocalContext.current.findActivity()
    val playing by connection.isPlaying.collectAsState()
    val position by connection.currentPosition.collectAsState()
    val duration by connection.duration.collectAsState()
    var dragPosition by remember(song.id) { mutableFloatStateOf(-1f) }
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

    DisposableEffect(activity, fullscreen) {
        val previousOrientation = activity?.requestedOrientation
        if (fullscreen) activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            if (fullscreen && previousOrientation != null) activity?.requestedOrientation = previousOrientation
        }
    }
    LaunchedEffect(fullscreen, showControls, playing, interactionCount, qualityMenu) {
        if (fullscreen && showControls && playing && !qualityMenu) {
            delay(4000)
            showControls = false
        }
    }

    val qualityButton: @Composable () -> Unit = {
        Box {
            IconButton(onClick = {
                qualityMenu = true
                interactionCount++
            }) {
                Icon(MetrolistIcons.Outlined.Settings, stringResource(Res.string.music_video_quality))
            }
            DropdownMenu(expanded = qualityMenu, onDismissRequest = { qualityMenu = false }) {
                MusicVideoQuality.entries.forEach { quality ->
                    val label =
                        if (quality == MusicVideoQuality.AUTO) {
                            stringResource(Res.string.music_video_quality_auto)
                        } else {
                            stringResource(Res.string.music_video_quality_resolution, quality.name.drop(1))
                        }
                    DropdownMenuItem(
                        text = { Text(if (Settings.shared.musicVideoQuality == quality) "✓ $label" else label) },
                        onClick = {
                            qualityMenu = false
                            interactionCount++
                            connection.changeMusicVideoResolution(quality)
                        },
                    )
                }
            }
        }
    }
    val seekBar: @Composable () -> Unit = {
        if (song.isLive) {
            Text(stringResource(Res.string.experimental_video_live), color = MaterialTheme.colorScheme.primary)
        } else {
            Slider(
                value = if (dragPosition >= 0f) dragPosition else progress,
                onValueChange = {
                    dragPosition = it
                    interactionCount++
                },
                onValueChangeFinished = {
                    if (duration > 0 && dragPosition >= 0f) connection.seekTo((duration * dragPosition).toLong())
                    dragPosition = -1f
                },
                enabled = duration > 0,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(if (dragPosition >= 0f) (duration * dragPosition).toLong() else position))
                Text(formatDuration(duration))
            }
        }
    }
    val playControls: @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                connection.skipToPrevious()
                interactionCount++
            }) {
                Icon(MetrolistIcons.Rounded.SkipPrevious, stringResource(Res.string.previous))
            }
            FilledIconButton(onClick = {
                connection.togglePlayPause()
                interactionCount++
            }, modifier = Modifier.size(64.dp)) {
                Icon(
                    if (playing) MetrolistIcons.Rounded.Pause else MetrolistIcons.Rounded.PlayArrow,
                    stringResource(if (playing) Res.string.pause else Res.string.play),
                    modifier = Modifier.size(32.dp),
                )
            }
            IconButton(onClick = {
                connection.skipToNext()
                interactionCount++
            }) {
                Icon(MetrolistIcons.Rounded.SkipNext, stringResource(Res.string.next))
            }
        }
    }
    val videoSurface: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier.background(Color.Black), contentAlignment = Alignment.Center) {
            if (hasVideo && player != null) {
                MusicVideoSurface(
                    player = player,
                    artworkUrl = song.thumbnail,
                    modifier = Modifier.fillMaxSize(),
                    fillContainer = true,
                    cropVideoToFill = false,
                    showControls = false,
                )
            } else {
                AsyncImage(model = song.thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            Box(
                Modifier.fillMaxSize().pointerInput(song.id, song.isLive, duration, fullscreen) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                            interactionCount++
                        },
                        onDoubleTap = { offset ->
                            videoDoubleTapSeek(connection.currentPosition.value, duration, offset.x >= size.width / 2f, song.isLive)
                                ?.let(connection::seekTo)
                            interactionCount++
                        },
                    )
                },
            )
            if (!fullscreen) {
                IconButton(onClick = {
                    fullscreen = true
                    showControls = true
                }, modifier = Modifier.align(Alignment.BottomEnd)) {
                    Icon(MetrolistIcons.Default.Fullscreen, stringResource(Res.string.enter_fullscreen), tint = Color.White)
                }
            }
        }
    }
    if (fullscreen) {
        Dialog(onDismissRequest = { fullscreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    videoSurface(Modifier.fillMaxSize())
                    if (showControls) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f)))
                        Row(Modifier.align(Alignment.TopStart).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { fullscreen = false }) {
                                Icon(MetrolistIcons.Rounded.KeyboardArrowDown, stringResource(Res.string.back))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    song.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                song.artists
                                    .firstOrNull()
                                    ?.name
                                    ?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                            qualityButton()
                        }
                        Box(Modifier.align(Alignment.Center)) { playControls() }
                        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 20.dp)) {
                            seekBar()
                        }
                    }
                }
            }
        }
    }
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(MetrolistIcons.Rounded.KeyboardArrowDown, stringResource(Res.string.back)) }
                Box(Modifier.weight(1f))
                qualityButton()
            }
            // Keep only one attached surface while fullscreen is visible.
            if (!fullscreen) videoSurface(Modifier.fillMaxWidth().aspectRatio(16f / 9f))
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(song.title, style = MaterialTheme.typography.headlineSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                song.artists
                    .firstOrNull()
                    ?.name
                    ?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                seekBar()
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { playControls() }
            }
        }
    }
}
