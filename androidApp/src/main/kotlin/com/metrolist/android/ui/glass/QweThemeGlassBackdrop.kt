package com.metrolist.android.ui.glass

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        val style =
            when (settings.glassStyle) {
                "CLEAR" -> QweGlassStyle.CLEAR
                "FROSTED" -> QweGlassStyle.FROSTED
                else -> QweGlassStyle.REGULAR
            }
        QweGlassOverlay(
            source = source,
            style = style,
            radius = radius,
            tint = Color.Transparent,
            dynamic = false,
            lightMode = settings.glassLightMode,
            refractionAllowed = true,
            modifier = modifier,
            blurStrength = settings.glassBlurStrength,
            sourceRevision = sourceRevision,
        )
    }
}
