package com.metrolist.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Filled action with a real platform backdrop when the Liquid Glass theme is enabled. */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = liquidGlassBackdropReady()
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

/** Intrinsic-width filled action for toolbars and empty-state retry controls. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassCompactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    LiquidGlassSurface(radius = 24.dp, modifier = modifier, enabled = glass) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            colors =
                if (glass) {
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (destructive) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.32f)
                            } else {
                                glassCardColor(MaterialTheme.colorScheme.surfaceContainer)
                            },
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.25f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                } else if (destructive) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                },
            border = if (glass) glassCardBorder() else null,
            content = content,
        )
    }
}

/** Outlined action that preserves its Material appearance when glass is unavailable. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    LiquidGlassSurface(radius = 24.dp, modifier = modifier, enabled = glass) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            colors =
                if (glass) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors()
                },
            border = if (glass) glassCardBorder() else ButtonDefaults.outlinedButtonBorder(enabled),
            content = content,
        )
    }
}

/** Tonal button colors keep text legible while revealing the platform glass behind the action. */
@Composable
fun liquidGlassTonalColors(selected: Boolean = false): ButtonColors =
    if (liquidGlassBackdropReady()) {
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

/** Tonal action preserving the Material fallback while using a real backdrop in glass mode. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    LiquidGlassSurface(radius = 24.dp, modifier = modifier, enabled = glass) {
        FilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            colors = liquidGlassTonalColors(),
            border = if (glass) glassCardBorder() else null,
            content = content,
        )
    }
}

/** Tonal icon action with the same fallback as Material's filled tonal icon button. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    val border = if (glass) glassCardBorder() else null
    LiquidGlassSurface(radius = 24.dp, modifier = modifier, enabled = glass) {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = if (border != null) Modifier.border(border, CircleShape) else Modifier,
            shapes = IconButtonDefaults.shapes(),
            colors =
                if (glass) {
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.25f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                } else {
                    IconButtonDefaults.filledTonalIconButtonColors()
                },
            content = content,
        )
    }
}

/** Circular action with a QWE backdrop, retaining ordinary IconButton behavior outside the theme. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    val border = if (glass) glassCardBorder() else null
    LiquidGlassSurface(radius = 24.dp, modifier = modifier) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = if (border != null) Modifier.border(border, CircleShape) else Modifier,
            shapes = IconButtonDefaults.shapes(),
            colors =
                if (glass) {
                    IconButtonDefaults.iconButtonColors(
                        containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    IconButtonDefaults.iconButtonColors()
                },
            content = content,
        )
    }
}

/** Toggle action with a translucent accent for its checked state. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    val border = if (glass) glassCardBorder() else null
    LiquidGlassSurface(radius = 24.dp, modifier = modifier, enabled = glass) {
        FilledTonalIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = if (border != null) Modifier.border(border, CircleShape) else Modifier,
            shapes = IconButtonDefaults.toggleableShapes(),
            colors =
                if (glass) {
                    IconButtonDefaults.filledTonalIconToggleButtonColors(
                        containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        checkedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                        checkedContentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.25f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                } else {
                    IconButtonDefaults.filledTonalIconToggleButtonColors()
                },
            content = content,
        )
    }
}

/** Backdrop for a slider's full touch target without changing its gesture or accessibility semantics. */
@Composable
fun LiquidGlassSliderContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    val shape = RoundedCornerShape(24.dp)
    val border = if (glass) glassCardBorder() else null
    val tint = if (glass) glassCardColor(MaterialTheme.colorScheme.surfaceContainer) else androidx.compose.ui.graphics.Color.Transparent
    LiquidGlassSurface(
        radius = 24.dp,
        modifier = if (glass) modifier.clip(shape) else modifier,
        enabled = glass,
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .then(if (glass) Modifier.background(tint, shape) else Modifier)
                .then(if (border != null) Modifier.border(border, shape) else Modifier),
        ) { content() }
    }
}

/** Text action with a bounded backdrop rather than an opaque filled rectangle. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val glass = liquidGlassBackdropReady()
    LiquidGlassSurface(radius = 24.dp, modifier = modifier, enabled = glass) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            colors =
                if (glass) {
                    ButtonDefaults.textButtonColors(
                        containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    ButtonDefaults.textButtonColors()
                },
            border = if (glass) glassCardBorder() else null,
            content = content,
        )
    }
}
