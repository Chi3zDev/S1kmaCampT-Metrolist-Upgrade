package com.metrolist.android.ui.glass

import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView

/**
 * Visual-only QWEA0 surface. The source must be a separate View, not the Compose host containing
 * this surface: sampling an ancestor Compose host can recurse or capture the wrong content.
 * The caller owns the source and must remove this view in lightweight mode.
 */
internal class QweGlassView(
    context: Context,
) : LiquidGlassView(context) {
    private var dynamicRequested = false

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        enablePressEffect = false
        enableChromaticAberration = false
        enableChromaticDispersion = false
        enableSensorHighlight = false
        enableAdaptiveTint = false
    }

    /** Radius is in pixels. Clear never enables the expensive refractive shader path. */
    fun configure(
        source: View,
        style: QweGlassStyle,
        radiusPx: Float,
        tintArgb: Int,
        dynamic: Boolean,
        refractionAllowed: Boolean = true,
    ) {
        require(source !== this) { "Glass cannot sample itself" }
        backdropSource = source
        material = if (style == QweGlassStyle.CLEAR) GlassMaterial.CLEAR else GlassMaterial.REGULAR
        useShaderPipeline = refractionAllowed && style != QweGlassStyle.CLEAR && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        blurAmount =
            when (style) {
                QweGlassStyle.CLEAR -> 0.05f
                QweGlassStyle.REGULAR -> 0.18f
                QweGlassStyle.FROSTED -> 0.4f
            }
        cornerRadius = radiusPx.coerceAtLeast(0f)
        glassTint = tintArgb
        dynamicRequested = dynamic
        updateDynamicState()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateDynamicState()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        updateDynamicState()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateDynamicState()
    }

    override fun onDetachedFromWindow() {
        enableDynamicBackground = false
        super.onDetachedFromWindow()
    }

    private fun updateDynamicState() {
        enableDynamicBackground = dynamicRequested && isAttachedToWindow && isShown && windowVisibility == View.VISIBLE
    }
}

internal enum class QweGlassStyle { CLEAR, REGULAR, FROSTED }
