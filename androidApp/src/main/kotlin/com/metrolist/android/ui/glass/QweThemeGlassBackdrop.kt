package com.metrolist.android.ui.glass

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.ThemeGlassBackdrop

internal class QweThemeGlassBackdrop(
    private val source: View,
    private val sourceRevision: Any,
) : ThemeGlassBackdrop {
    @Composable
    override fun Background(
        radius: Dp,
        modifier: Modifier,
    ) {
        val settings = Settings.shared
        val style = qweGlassStyle(settings.glassStyle)
        val host = LocalView.current
        // A Compose dialog/popup lives in another window, so the activity root cannot contain
        // this glass view. It is a safe source for the actual moving screen behind that window.
        // In the activity itself, keep the separate wallpaper source: sampling an ancestor of a
        // Compose AndroidView can recurse through its rendering tree.
        var attachmentRevision by remember(host, source) { mutableStateOf(0) }
        DisposableEffect(host, source) {
            val listener =
                object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(view: View) {
                        attachmentRevision++
                    }

                    override fun onViewDetachedFromWindow(view: View) {
                        attachmentRevision++
                    }
                }
            host.addOnAttachStateChangeListener(listener)
            source.addOnAttachStateChangeListener(listener)
            onDispose {
                host.removeOnAttachStateChangeListener(listener)
                source.removeOnAttachStateChangeListener(listener)
            }
        }
        // Read the revision so moving a popup between windows updates its source.
        @Suppress("UNUSED_VARIABLE")
        val revision = attachmentRevision
        val activityRoot = source.rootView
        val separateWindow =
            canCaptureActivityWindow(
                hostAttached = host.isAttachedToWindow,
                sourceAttached = source.isAttachedToWindow,
                sameRoot = host.rootView === activityRoot,
            )
        val captureSource = if (separateWindow) activityRoot else source
        QweGlassOverlay(
            source = captureSource,
            style = style,
            radius = radius,
            tint = Color.Transparent,
            dynamic = separateWindow,
            lightMode = settings.glassLightMode,
            refractionAllowed = true,
            modifier = modifier,
            blurStrength = settings.glassBlurStrength,
            sourceRevision = sourceRevision,
        )
    }
}
