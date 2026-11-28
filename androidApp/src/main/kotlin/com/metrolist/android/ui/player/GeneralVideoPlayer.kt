package com.metrolist.android.ui.player

import androidx.compose.foundation.background
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.android.playback.PlayerConnection
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.back
import com.metrolist.shared.generated.resources.experimental_video_live
import com.metrolist.shared.generated.resources.next
import com.metrolist.shared.generated.resources.pause
import com.metrolist.shared.generated.resources.play
import com.metrolist.shared.generated.resources.previous
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.isLive
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.utils.formatDuration
import org.jetbrains.compose.resources.stringResource

/** Video-specific presentation, reusing the active Media3 player rather than replacing playback. */
@Composable
fun GeneralVideoPlayer(
    song: SongItem,
    connection: PlayerConnection,
    player: Player?,
    hasVideo: Boolean,
    onClose: () -> Unit,
) {
    var fullscreen by rememberSaveable(song.id) { mutableStateOf(false) }
    val playing by connection.isPlaying.collectAsState()
    val position by connection.currentPosition.collectAsState()
    val duration by connection.duration.collectAsState()
    var dragPosition by remember(song.id) { mutableFloatStateOf(-1f) }
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val videoSurface: @Composable (Modifier, Boolean) -> Unit = { modifier, expanded ->
        Box(modifier.background(Color.Black), contentAlignment = Alignment.Center) {
            if (hasVideo && player != null) {
                MusicVideoSurface(
                    player = player,
                    artworkUrl = song.thumbnail,
                    modifier = Modifier.fillMaxSize(),
                    fillContainer = true,
                    cropVideoToFill = false,
                    onFullscreenClick = if (expanded) null else ({ fullscreen = true }),
                )
            } else {
                AsyncImage(model = song.thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
    }
    if (fullscreen) {
        Dialog(onDismissRequest = { fullscreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                videoSurface(Modifier.fillMaxSize(), true)
                IconButton(onClick = { fullscreen = false }, modifier = Modifier.align(Alignment.TopStart).statusBarsPadding()) {
                    Icon(MetrolistIcons.Rounded.KeyboardArrowDown, stringResource(Res.string.back), tint = Color.White)
                }
                FilledIconButton(
                    onClick = connection::togglePlayPause,
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                ) {
                    Icon(
                        if (playing) MetrolistIcons.Rounded.Pause else MetrolistIcons.Rounded.PlayArrow,
                        stringResource(if (playing) Res.string.pause else Res.string.play),
                    )
                }
            }
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
    ) {
        IconButton(onClick = onClose) { Icon(MetrolistIcons.Rounded.KeyboardArrowDown, stringResource(Res.string.back)) }
        // Keep only one attached surface while the fullscreen dialog is visible.
        if (!fullscreen) videoSurface(Modifier.fillMaxWidth().aspectRatio(16f / 9f), false)
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(song.title, style = MaterialTheme.typography.headlineSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            song.artists
                .firstOrNull()
                ?.name
                ?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            if (song.isLive) {
                Text(stringResource(Res.string.experimental_video_live), color = MaterialTheme.colorScheme.primary)
            } else {
                Slider(
                    value = if (dragPosition >= 0f) dragPosition else progress,
                    onValueChange = { dragPosition = it },
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = connection::skipToPrevious) {
                    Icon(MetrolistIcons.Rounded.SkipPrevious, stringResource(Res.string.previous))
                }
                FilledIconButton(onClick = connection::togglePlayPause, modifier = Modifier.size(64.dp)) {
                    Icon(
                        if (playing) MetrolistIcons.Rounded.Pause else MetrolistIcons.Rounded.PlayArrow,
                        stringResource(if (playing) Res.string.pause else Res.string.play),
                        modifier = Modifier.size(32.dp),
                    )
                }
                IconButton(onClick = connection::skipToNext) {
                    Icon(MetrolistIcons.Rounded.SkipNext, stringResource(Res.string.next))
                }
            }
        }
    }
}
