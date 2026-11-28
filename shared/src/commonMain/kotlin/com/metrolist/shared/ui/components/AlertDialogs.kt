package com.metrolist.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.metrolist.shared.settings.Settings
import androidx.compose.material3.AlertDialog as MaterialAlertDialog
import androidx.compose.material3.BasicAlertDialog as MaterialBasicAlertDialog

// Android dialog windows replace LocalDensity, so restore the app appearance scale inside them.

@Composable
fun BasicAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    MaterialBasicAlertDialog(
        onDismissRequest,
        modifier.popupMotion(),
        properties,
    ) {
        WithDensity(density, content)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    outlineActions: Boolean = true,
) {
    val density = LocalDensity.current
    val scaled = { slot: @Composable () -> Unit -> @Composable { WithDensity(density, slot) } }
    // Material buttons only draw a faint focus layer; outline the focused action for remotes and keyboards.
    val action = { slot: @Composable () -> Unit ->
        @Composable {
            Box(if (outlineActions) Modifier.focusOutline(CircleShape) else Modifier) { WithDensity(density, slot) }
        }
    }
    val backdrop = LocalThemeGlassBackdrop.current.takeIf { Settings.shared.liquidGlassTheme }
    if (backdrop != null) {
        val tint = glassCardColor(containerColor)
        MaterialBasicAlertDialog(onDismissRequest, modifier.popupMotion(), properties) {
            WithDensity(density) {
                Surface(
                    modifier = Modifier.widthIn(min = 280.dp, max = 560.dp),
                    shape = shape,
                    color = Color.Transparent,
                    tonalElevation = tonalElevation,
                    border = glassCardBorder(),
                ) {
                    Box {
                        backdrop.Background(radius = 28.dp, modifier = Modifier.matchParentSize())
                        Box(Modifier.matchParentSize().background(tint))
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            if (icon != null) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CompositionLocalProvider(LocalContentColor provides iconContentColor) { icon() }
                                }
                            }
                            if (title != null) {
                                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) { title() }
                                }
                            }
                            if (text != null) {
                                CompositionLocalProvider(LocalContentColor provides textContentColor) {
                                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() }
                                }
                            }
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                dismissButton?.let { action(it)() }
                                action(confirmButton)()
                            }
                        }
                    }
                }
            }
        }
        return
    }
    MaterialAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = action(confirmButton),
        modifier = modifier.popupMotion(),
        dismissButton = dismissButton?.let(action),
        icon = icon?.let(scaled),
        title = title?.let(scaled),
        text = text?.let(scaled),
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}

@Composable
private fun WithDensity(
    density: Density,
    content: @Composable () -> Unit,
) = CompositionLocalProvider(LocalDensity provides density, content = content)
