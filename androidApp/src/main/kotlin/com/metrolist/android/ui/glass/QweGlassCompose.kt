package com.metrolist.android.ui.glass

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView

/** A single dedicated source; do not wrap every row or sample the host containing glass views. */
@Composable
internal fun QweBackdropSource(
    onViewChanged: (View?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val parent = rememberCompositionContext()
    val latestContent by rememberUpdatedState(content)
    val latestCallback by rememberUpdatedState(onViewChanged)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ComposeView(context).apply {
                setParentCompositionContext(parent)
                setContent { latestContent() }
                latestCallback(this)
            }
        },
        onRelease = { view ->
            latestCallback(null)
            view.disposeComposition()
        },
    )
}

@Composable
internal fun rememberQweGlassAvailable(
    enabled: Boolean,
    lightMode: Boolean,
): Boolean {
    val context = LocalContext.current
    val lowRam = remember(context) { (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).isLowRamDevice }
    if (!enabled || !shouldAttemptQweGlass(Build.VERSION.SDK_INT, lowRam, lightMode, hasSource = true)) return false
    return remember {
        // The library's CPU fallback can load nativegauss even on newer Android.
        runCatching { System.loadLibrary("nativegauss") }.isSuccess
    }
}

/**
 * Draws QWEA0's real View-based glass on a separate source, behind Compose controls.
 * Callers must provide a normal surface fallback whenever this returns without a view.
 */
@Composable
internal fun QweGlassOverlay(
    source: View?,
    style: QweGlassStyle,
    radius: Dp,
    tint: Color,
    dynamic: Boolean,
    lightMode: Boolean,
    refractionAllowed: Boolean,
    modifier: Modifier = Modifier,
    onViewChanged: (View?) -> Unit = {},
) {
    if (!rememberQweGlassAvailable(enabled = source != null, lightMode = lightMode)) return
    source ?: return
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    val tintArgb = tint.toArgb()
    val latestCallback by rememberUpdatedState(onViewChanged)
    AndroidView(
        modifier = modifier,
        factory = { QweGlassView(it).also { view -> latestCallback(view) } },
        update = { view -> view.configure(source, style, radiusPx, tintArgb, dynamic, refractionAllowed) },
        onRelease = { view ->
            view.enableDynamicBackground = false
            view.backdropSource = null
            latestCallback(null)
        },
    )
}
