package com.metrolist.shared.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.chip_content_description
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.tv.tvFocusHighlight
import com.metrolist.shared.ui.utils.carouselScroll
import org.jetbrains.compose.resources.stringResource

private val ChipShelfHeight = 48.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ChipShelf(
    options: List<T>,
    selectedOption: T?,
    onSelectionChange: (T?) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    allLabel: String? = null,
    pinnedLabel: String? = null,
    onPinnedClick: () -> Unit = {},
    selectionRequired: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp),
    leadingContent: (@Composable () -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = ChipShelfHeight)
                .padding(horizontal = 4.dp)
                .clipToBounds()
                .focusGroup()
                .selectableGroup()
                .carouselScroll(scrollState)
                .horizontalScroll(scrollState)
                .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.invoke()
        pinnedLabel?.let { text ->
            ShelfChip(
                text = text,
                selected = true,
                onClick = onPinnedClick,
            )
        }
        allLabel?.let { text ->
            ShelfChip(
                text = text,
                selected = selectedOption == null,
                onClick = { onSelectionChange(null) },
            )
        }
        options.forEach { option ->
            val selected = selectedOption == option
            ShelfChip(
                text = label(option),
                selected = selected,
                onClick = {
                    onSelectionChange(
                        if (selected && !selectionRequired) null else option,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShelfChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerHeight = ButtonDefaults.ExtraSmallContainerHeight
    val chipDescription = stringResource(Res.string.chip_content_description, text)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) {
        if (selected) bringIntoViewRequester.bringIntoView()
    }
    LiquidGlassChip(
        selected = selected,
        onClick = onClick,
        modifier =
            Modifier.heightIn(min = containerHeight).bringIntoViewRequester(bringIntoViewRequester).clearAndSetSemantics {
                contentDescription = chipDescription
                role = Role.Button
                this.selected = selected
                onClick {
                    onClick()
                    true
                }
            },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val glassBorder = glassCardBorder()
    val themeGlass = Settings.shared.liquidGlassTheme
    val backdrop = LocalThemeGlassBackdrop.current
    var visible by remember { mutableStateOf(false) }
    Box(
        Modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            visible = bounds.width > 0f && bounds.height > 0f
        },
    ) {
        // Shelf chips may be eagerly composed; avoid renderers for clipped, off-screen chips.
        if (themeGlass && visible) {
            backdrop?.Background(radius = ButtonDefaults.ExtraSmallContainerHeight / 2, modifier = Modifier.matchParentSize())
        }
        ToggleButton(
            checked = selected,
            onCheckedChange = { onClick() },
            enabled = enabled,
            shapes = ToggleButtonDefaults.shapesFor(ButtonDefaults.ExtraSmallContainerHeight),
            colors =
                ToggleButtonDefaults.colors(
                    containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    checkedContainerColor =
                        if (themeGlass) {
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.32f,
                            )
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                    checkedContentColor =
                        if (themeGlass) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                ),
            contentPadding = ButtonDefaults.ExtraSmallContentPadding,
            modifier =
                modifier
                    .heightIn(min = ButtonDefaults.ExtraSmallContainerHeight)
                    .then(if (glassBorder != null) Modifier.border(glassBorder, ToggleButtonDefaults.shape) else Modifier)
                    .tvFocusHighlight(ToggleButtonDefaults.shape)
                    .semantics { this.selected = selected },
        ) {
            label()
        }
    }
}
