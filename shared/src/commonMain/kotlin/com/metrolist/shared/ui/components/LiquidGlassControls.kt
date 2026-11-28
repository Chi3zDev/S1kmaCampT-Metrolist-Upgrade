package com.metrolist.shared.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings

/** Filled action with a real platform backdrop when the Liquid Glass theme is enabled. */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = Settings.shared.liquidGlassTheme
    LiquidGlassSurface(radius = 32.dp, modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors =
                if (glass) {
                    ButtonDefaults.buttonColors(
                        containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            border = if (glass) glassCardBorder() else null,
            content = content,
        )
    }
}

/** Tonal button colors keep text legible while revealing the platform glass behind the action. */
@Composable
fun liquidGlassTonalColors(selected: Boolean = false): ButtonColors =
    if (Settings.shared.liquidGlassTheme) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor =
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
                } else {
                    glassCardColor(MaterialTheme.colorScheme.surfaceContainer)
                },
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.25f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
