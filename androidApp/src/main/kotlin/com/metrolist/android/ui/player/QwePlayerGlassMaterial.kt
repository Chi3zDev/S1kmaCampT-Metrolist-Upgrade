package com.metrolist.android.ui.player

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.metrolist.android.ui.glass.QweBackdropSource
import com.metrolist.android.ui.glass.QweGlassOverlay
import com.metrolist.android.ui.glass.QweGlassStyle
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.player.PlayerGlassMaterial
import kotlin.math.roundToInt

/** Samples the player background only; controls and video never become the source. */
internal class QwePlayerGlassMaterial : PlayerGlassMaterial {
    override val sourceModifier: Modifier = Modifier
    private var source by mutableStateOf<View?>(null)
    private var bounds by mutableStateOf<CardBounds?>(null)

    @Composable
    override fun Background(content: @Composable BoxScope.() -> Unit) {
        QweBackdropSource(
            onViewChanged = { source = it },
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }

    @Composable
    override fun Overlay(modifier: Modifier) {
        val sourceView = source ?: return
        val card = bounds ?: return
        val density = LocalDensity.current
        val sourceLocation = IntArray(2)
        sourceView.getLocationInWindow(sourceLocation)
        val x = (card.left - sourceLocation[0]).roundToInt()
        val y = (card.top - sourceLocation[1]).roundToInt()
        Box(modifier) {
            QweGlassOverlay(
                source = sourceView,
                style = QweGlassStyle.REGULAR,
                radius = card.radius,
                tint = card.tint,
                dynamic = true,
                lightMode = Settings.shared.glassLightMode,
                refractionAllowed = true,
                modifier =
                    Modifier
                        .absoluteOffset { IntOffset(x, y) }
                        .size(with(density) { card.width.toDp() }, with(density) { card.height.toDp() }),
            )
        }
    }

    @Composable
    override fun cardModifier(
        topInset: Dp,
        bottomInset: Dp,
        cornerRadius: Dp,
        contentColor: Color,
    ): Modifier {
        val token = remember { Any() }
        val density = LocalDensity.current
        val topPx = with(density) { topInset.toPx() }
        val bottomPx = with(density) { bottomInset.toPx() }
        val tintStrength = (Settings.shared.glassTintStrength.coerceIn(0f, 100f) / 100f * 0.35f).coerceIn(0.1f, 0.35f)
        val tint = if (contentColor.luminance() > 0.5f) Color.Black.copy(alpha = tintStrength) else Color.White.copy(alpha = tintStrength)
        DisposableEffect(token) {
            onDispose { if (bounds?.token === token) bounds = null }
        }
        return Modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInWindow()
            val top = topPx.coerceIn(0f, coordinates.size.height.toFloat())
            val bottom = (coordinates.size.height - bottomPx).coerceIn(top, coordinates.size.height.toFloat())
            bounds =
                CardBounds(
                    token = token,
                    left = position.x,
                    top = position.y + top,
                    width = coordinates.size.width.toFloat(),
                    height = bottom - top,
                    radius = cornerRadius,
                    tint = tint,
                )
        }
    }

    private data class CardBounds(
        val token: Any,
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val radius: Dp,
        val tint: Color,
    )
}
