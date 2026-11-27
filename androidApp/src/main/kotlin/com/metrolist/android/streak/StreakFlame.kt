package com.metrolist.android.streak

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import org.json.JSONObject
import kotlin.math.min

internal data class StreakFlameState(
    val animate: Boolean,
    val extinguished: Boolean,
    val particles: Boolean,
)

private data class FlameLayer(
    val path: Path,
    val top: Color,
    val bottom: Color,
)

private data class FlameSpark(
    val x: Float,
    val y: Float,
    val radius: Float,
)

private data class FlameArtwork(
    val glowX: Float,
    val glowY: Float,
    val glowRx: Float,
    val glowRy: Float,
    val glow: Color,
    val layers: List<FlameLayer>,
    val highlight: Path,
    val highlightAlpha: Float,
    val sparks: List<FlameSpark>,
    val halo: Color?,
)

private fun JSONObject.number(key: String): Float = getDouble(key).toFloat()

private fun JSONObject.color(key: String): Color = Color(android.graphics.Color.parseColor(getString(key)))

private fun JSONObject.path(): Path = PathParser().parsePathString(getString("d")).toPath()

private fun parseFlameArtwork(json: String): List<FlameArtwork> {
    val levels = JSONObject(json).getJSONArray("levels")
    return List(levels.length()) { index ->
        val level = levels.getJSONObject(index)
        val glow = level.getJSONObject("glow")
        val layers =
            listOf("outer", "mid", "inner", "core").map { key ->
                val layer = level.getJSONObject(key)
                val top = layer.color(if (key == "core") "color" else "top")
                FlameLayer(layer.path(), top, if (key == "core") top else layer.color("bottom"))
            }
        val highlight = level.getJSONObject("highlight")
        val sparks = level.getJSONArray("sparks")
        FlameArtwork(
            glowX = glow.number("cx"),
            glowY = glow.number("cy"),
            glowRx = glow.number("rx"),
            glowRy = glow.number("ry"),
            glow = glow.color("color"),
            layers = layers,
            highlight = highlight.path(),
            highlightAlpha = highlight.number("alpha"),
            sparks =
                List(sparks.length()) { i ->
                    val spark = sparks.getJSONObject(i)
                    FlameSpark(spark.number("cx"), spark.number("cy"), spark.number("r"))
                },
            halo = level.optJSONObject("halo")?.color("color"),
        )
    }
}

/** Seven original path-based flames. Coordinates use the supplied viewBox and baseline y=136. */
@Composable
internal fun StreakFlame(
    level: Int,
    size: Dp,
    state: StreakFlameState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val artwork =
        remember(context) {
            parseFlameArtwork(
                context.assets
                    .open("api-level.json")
                    .bufferedReader()
                    .use { it.readText() },
            )
        }[level.coerceIn(1, 7) - 1]
    // No infinite transition is composed when motion is disabled or the flame is extinguished.
    val transition = if (state.animate && !state.extinguished) rememberInfiniteTransition(label = "streakFlame") else null

    @Composable
    fun phase(
        duration: Int,
        label: String,
    ) = transition?.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(duration, easing = LinearEasing), RepeatMode.Restart),
        label = label,
    )
    val outer = phase(900, "outer")
    val mid = phase(800, "mid")
    val inner = phase(700, "inner")
    val core = phase(550, "core")
    val glow = phase(1500, "glow")
    val sparks = phase(1600, "sparks")
    val halo = phase(12000, "halo")
    val grayscale = remember { ColorMatrix().apply { setToSaturation(0f) } }
    Canvas(modifier.size(size)) {
        val factor = min(this.size.width / 140f, this.size.height / 180f)
        val left = (this.size.width - 140f * factor) / 2f + 10f * factor
        val top = (this.size.height - 180f * factor) / 2f + 40f * factor
        if (state.extinguished) {
            drawContext.canvas.saveLayer(
                Rect(Offset.Zero, this.size),
                Paint().apply {
                    colorFilter = ColorFilter.colorMatrix(grayscale)
                    alpha = 0.6f
                },
            )
        }
        translate(left, top) {
            scale(factor, factor, pivot = Offset.Zero) {
                val anchor = Offset(60f, 136f)

                fun wave(phase: Float?) = kotlin.math.sin((phase ?: 0f) * 2f * kotlin.math.PI.toFloat())
                if (state.animate && !state.extinguished) {
                    artwork.halo?.let { color ->
                        rotate((halo?.value ?: 0f) * 360f, pivot = Offset(60f, 84f)) {
                            drawArc(color.copy(alpha = 0.25f), 0f, 265f, false, Offset(-36f, -12f), Size(192f, 192f), style = Stroke(2f))
                        }
                    }
                    val pulse = 0.8f + 0.2f * wave(glow?.value)
                    scale(1f, artwork.glowRy / artwork.glowRx, pivot = Offset(artwork.glowX, artwork.glowY)) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    listOf(artwork.glow.copy(alpha = 0.24f * pulse), Color.Transparent),
                                    center = Offset(artwork.glowX, artwork.glowY),
                                    radius = artwork.glowRx,
                                ),
                            radius = artwork.glowRx,
                            center = Offset(artwork.glowX, artwork.glowY),
                        )
                    }
                }
                artwork.layers.forEachIndexed { index, layer ->
                    val p =
                        when (index) {
                            0 -> outer
                            1 -> mid
                            2 -> inner
                            else -> core
                        }
                    val w = wave(p?.value?.plus(index * 0.12f))
                    val sx = if (index == 3) 1f + w * 0.06f else 1f - w * 0.025f
                    val sy = if (index == 3) 1f + w * 0.06f else 1.005f + w * 0.035f
                    scale(sx, sy, pivot = anchor) {
                        rotate(if (index == 2) w * 2f else 0f, pivot = anchor) {
                            val bounds = layer.path.getBounds()
                            drawPath(layer.path, Brush.verticalGradient(listOf(layer.top, layer.bottom), bounds.top, bounds.bottom))
                        }
                    }
                }
                val highlightAlpha = if (state.animate) 0.25f + 0.1f * wave(glow?.value) else artwork.highlightAlpha
                drawPath(artwork.highlight, Color.White.copy(alpha = highlightAlpha))
                if (state.animate && state.particles && !state.extinguished) {
                    artwork.sparks.forEachIndexed { index, spark ->
                        val progress = ((sparks?.value ?: 0f) + index * 0.21f) % 1f
                        drawCircle(artwork.glow.copy(alpha = 1f - progress), spark.radius, Offset(spark.x, spark.y - 24f * progress))
                    }
                }
            }
        }
        if (state.extinguished) drawContext.canvas.restore()
    }
}
