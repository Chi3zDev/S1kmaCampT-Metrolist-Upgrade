package com.metrolist.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A popup with a platform backdrop; retain the ordinary Material menu when it is unavailable. */
@Composable
fun LiquidGlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    if (!glass) {
        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, content = content)
    } else {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.Transparent,
            border = glassCardBorder(),
        ) {
            LiquidGlassSurface(radius = 16.dp) {
                Column(Modifier.background(glassCardColor(MaterialTheme.colorScheme.surfaceContainerHigh)), content = content)
            }
        }
    }
}
