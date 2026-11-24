package com.metrolist.android.ui.player

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.volume
import com.metrolist.shared.settings.SliderStyle
import com.metrolist.shared.ui.components.sliders.PlaybackProgressSlider
import com.metrolist.shared.ui.icons.MetrolistIcons
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

internal fun systemVolumeFraction(
    value: Int,
    minimum: Int,
    maximum: Int,
): Float = if (maximum <= minimum) 0f else ((value.coerceIn(minimum, maximum) - minimum).toFloat() / (maximum - minimum))

internal fun systemVolumeFromFraction(
    value: Float,
    minimum: Int,
    maximum: Int,
): Int {
    if (maximum <= minimum) return minimum
    val safe = if (value.isFinite()) value.coerceIn(0f, 1f) else 0f
    return minimum + (safe * (maximum - minimum)).roundToInt()
}

/** Controls Android's local music stream, not the player's software gain or remote Cast volume. */
@Composable
internal fun SystemMusicVolumeControl(
    modifier: Modifier = Modifier,
    contentColor: Color,
    active: Boolean,
) {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager } ?: return
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val minimum =
        remember(manager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) manager.getStreamMinVolume(AudioManager.STREAM_MUSIC) else 0
        }
    val maximum = remember(manager) { manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(minimum) }
    var current by remember(manager) { mutableIntStateOf(manager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    // The local hardware keys can change volume outside the app. Poll only while this control is visible
    // and the lifecycle is started; unchanged integer values do not trigger recomposition.
    LaunchedEffect(manager, lifecycle, active) {
        if (!active) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                current = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
                delay(750)
            }
        }
    }
    val label = stringResource(Res.string.volume)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            MetrolistIcons.AutoMirrored.Filled.VolumeDown,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.65f),
            modifier = Modifier.size(18.dp),
        )
        PlaybackProgressSlider(
            value = systemVolumeFraction(current, minimum, maximum),
            onValueChange = { fraction ->
                val next = systemVolumeFromFraction(fraction, minimum, maximum)
                if (next != current) {
                    manager.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                    current = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
                }
            },
            onValueChangeFinished = {},
            isPlaying = false,
            enabled = active && maximum > minimum,
            style = SliderStyle.SLIM,
            trackThicknessDp = 4f,
            showThumb = false,
            animate = false,
            colors =
                SliderDefaults.colors(
                    thumbColor = contentColor,
                    activeTrackColor = contentColor,
                    inactiveTrackColor = contentColor.copy(alpha = 0.3f),
                ),
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
        )
        Icon(
            MetrolistIcons.AutoMirrored.Filled.VolumeUp,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.65f),
            modifier = Modifier.size(18.dp),
        )
    }
}
