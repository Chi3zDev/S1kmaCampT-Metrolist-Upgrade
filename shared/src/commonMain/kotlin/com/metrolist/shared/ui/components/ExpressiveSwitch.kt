/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.shared.ui.haptics.HapticEvent
import com.metrolist.shared.ui.haptics.rememberMetrolistHaptics
import com.metrolist.shared.ui.icons.MetrolistIcons

@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptics = rememberMetrolistHaptics()
    val glass = liquidGlassBackdropReady()
    val c = MaterialTheme.colorScheme
    LiquidGlassSurface(radius = 16.dp, modifier = modifier) {
        Switch(
            checked = checked,
            onCheckedChange =
                onCheckedChange?.let { onChange ->
                    { value ->
                        haptics.toggle(value)
                        onChange(value)
                    }
                },
            enabled = enabled,
            colors =
                if (glass) {
                    SwitchDefaults.colors(
                        checkedThumbColor = c.primary,
                        checkedTrackColor = c.primary.copy(alpha = 0.28f),
                        checkedBorderColor = c.outline.copy(alpha = 0.5f),
                        checkedIconColor = c.onPrimary,
                        uncheckedThumbColor = c.onSurfaceVariant,
                        uncheckedTrackColor = c.surfaceVariant.copy(alpha = 0.28f),
                        uncheckedBorderColor = c.outline.copy(alpha = 0.5f),
                        uncheckedIconColor = c.surface,
                    )
                } else {
                    SwitchDefaults.colors()
                },
            thumbContent = {
                if (checked) {
                    Icon(
                        imageVector = MetrolistIcons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                } else {
                    Icon(
                        imageVector = MetrolistIcons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            },
        )
    }
}
