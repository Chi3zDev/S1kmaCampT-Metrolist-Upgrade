@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.add_to_favorites
import com.metrolist.shared.generated.resources.remove_from_favorites
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.ui.tv.tvFocusHighlight
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlaylistHeaderActions(
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    playLabel: String,
    shuffleLabel: String,
    showPlayback: Boolean,
    isFavorite: Boolean,
    onFavorite: (() -> Unit)?,
    buttonHeight: Dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPlayback) {
            HeaderPlaybackButtons(onPlayAll, onShuffle, playLabel, shuffleLabel, buttonHeight, Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (onFavorite != null) {
            LiquidGlassSurface(radius = buttonHeight / 2) {
                ToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { onFavorite() },
                    modifier = Modifier.height(buttonHeight).width(48.dp).tvFocusHighlight(CircleShape),
                    shapes = ToggleButtonDefaults.shapesFor(buttonHeight).withStaticCorners(),
                    contentPadding = PaddingValues(0.dp),
                    colors =
                        if (Settings.shared.liquidGlassTheme) {
                            ToggleButtonDefaults.colors(
                                containerColor = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                checkedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                                checkedContentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        } else {
                            ToggleButtonDefaults.colors()
                        },
                    border = if (Settings.shared.liquidGlassTheme) glassCardBorder() else null,
                ) {
                    Icon(
                        if (isFavorite) MetrolistIcons.Filled.Favorite else MetrolistIcons.Default.FavoriteBorder,
                        stringResource(if (isFavorite) Res.string.remove_from_favorites else Res.string.add_to_favorites),
                        Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun HeaderPlaybackButtons(
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    playLabel: String,
    shuffleLabel: String,
    buttonHeight: Dp,
    modifier: Modifier = Modifier,
    playFirst: Boolean = false,
    tonalShuffle: Boolean = false,
    enabled: Boolean = true,
) {
    val labels = if (playFirst) listOf(playLabel, shuffleLabel) else listOf(shuffleLabel, playLabel)
    val icons =
        if (playFirst) {
            listOf(
                MetrolistIcons.Default.PlayArrow,
                MetrolistIcons.Rounded.Shuffle,
            )
        } else {
            listOf(MetrolistIcons.Rounded.Shuffle, MetrolistIcons.Default.PlayArrow)
        }
    val callbacks = if (playFirst) listOf(onPlayAll, onShuffle) else listOf(onShuffle, onPlayAll)
    LiquidGlassSurface(radius = buttonHeight / 2, modifier = modifier) {
        ButtonGroup(
            overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
            modifier = Modifier.fillMaxWidth(),
            expandedRatio = 1f,
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            labels.forEachIndexed { index, label ->
                customItem(
                    buttonGroupContent = {
                        val interactionSource = remember { MutableInteractionSource() }
                        val shapes =
                            if (index == 0) {
                                ButtonGroupDefaults.connectedLeadingButtonShapes()
                            } else {
                                ButtonGroupDefaults.connectedTrailingButtonShapes()
                            }.withStaticCorners()
                        val buttonModifier =
                            Modifier
                                .weight(1f)
                                .animateWidth(interactionSource, 12.dp)
                                .height(buttonHeight)
                                .tvFocusHighlight(shapes.shape)
                        val buttonShapes = ButtonDefaults.shapes(shape = shapes.shape, pressedShape = shapes.pressedShape)
                        val content: @Composable RowScope.() -> Unit = {
                            Icon(icons[index], null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                        }
                        if (tonalShuffle && index == if (playFirst) 1 else 0) {
                            FilledTonalButton(
                                onClick = callbacks[index],
                                enabled = enabled,
                                interactionSource = interactionSource,
                                modifier = buttonModifier,
                                shapes = buttonShapes,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = liquidGlassTonalColors(),
                                border = if (Settings.shared.liquidGlassTheme) glassCardBorder() else null,
                                content = content,
                            )
                        } else {
                            Button(
                                onClick = callbacks[index],
                                enabled = enabled,
                                interactionSource = interactionSource,
                                modifier = buttonModifier,
                                shapes = buttonShapes,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = if (Settings.shared.liquidGlassTheme) liquidGlassTonalColors() else ButtonDefaults.buttonColors(),
                                border = if (Settings.shared.liquidGlassTheme) glassCardBorder() else null,
                                content = content,
                            )
                        }
                    },
                    menuContent = {
                        DropdownMenuItem(text = { Text(label) }, onClick = callbacks[index], enabled = enabled)
                    },
                )
            }
        }
    }
}
