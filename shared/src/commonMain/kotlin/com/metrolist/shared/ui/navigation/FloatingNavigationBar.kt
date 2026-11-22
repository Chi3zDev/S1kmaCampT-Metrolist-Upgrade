@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.focusOutline
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

val LocalFloatingNavBar = compositionLocalOf { false }
val LocalGlassNavbarBottomOffset = compositionLocalOf { 0.dp }
val LocalGlassNavigationEnabled = compositionLocalOf { false }
val LocalGlassNavigationState = compositionLocalOf<HazeState?> { null }
val LocalGlassNavigationCollapsed = compositionLocalOf { false }
val LocalExpandGlassNavigation = compositionLocalOf<() -> Unit> { {} }

private val FloatingBarHeight = 64.dp
private val FloatingItemHeight = 48.dp
private val FloatingBarShadow = 6.dp
private val NavSpring = spring<Float>(dampingRatio = 0.9f, stiffness = 700f)

@Composable
internal fun <T> FloatingNavigation(
    items: List<AdaptiveNavigationItem<T>>,
    selectedKey: T?,
    onSelect: (T) -> Unit,
    navigationVisible: Boolean,
    separateKey: T?,
    itemModifier: @Composable (T) -> Modifier,
    onBarHeightChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = navigationVisible,
        modifier = modifier,
        enter =
            expandVertically(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                expandFrom = Alignment.Bottom,
            ),
        exit =
            shrinkVertically(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                shrinkTowards = Alignment.Bottom,
            ),
    ) {
        FloatingNavigationBar(
            items = items,
            selectedKey = selectedKey,
            onSelect = onSelect,
            separateKey = separateKey,
            itemModifier = itemModifier,
            modifier =
                Modifier.onSizeChanged { size ->
                    onBarHeightChanged(with(density) { size.height.toDp() })
                },
        )
    }
}

@Composable
private fun <T> FloatingNavigationBar(
    items: List<AdaptiveNavigationItem<T>>,
    selectedKey: T?,
    onSelect: (T) -> Unit,
    separateKey: T?,
    itemModifier: @Composable (T) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val glassState = LocalGlassNavigationState.current
    val glassEnabled = LocalGlassNavigationEnabled.current
    val collapsed = glassEnabled && LocalGlassNavigationCollapsed.current
    val expand = LocalExpandGlassNavigation.current
    val pillItems = if (glassEnabled) items else items.filter { it.key != separateKey }
    val separateItem = if (glassEnabled) null else items.firstOrNull { it.key == separateKey }
    val settings = Settings.shared
    val radius = (settings.glassBlurStrength.coerceIn(0f, 100f) / 100f * when (settings.glassStyle) { "FROSTED" -> 24f; "CLEAR" -> 8f; else -> 20f }).dp
    val tintAlpha = (settings.glassTintStrength.coerceIn(0f, 100f) / 100f * when (settings.glassStyle) { "FROSTED" -> 0.9f; "CLEAR" -> 0.45f; else -> 0.7f }).coerceIn(0.12f, 0.9f)
    val glassTint = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = tintAlpha)
    val glassShape = remember(settings.glassCornerRoundness) { RoundedCornerShape((12f + settings.glassCornerRoundness.coerceIn(0f, 100f) * 0.38f).toInt()) }
    val glassStyle = remember(radius) { HazeBlurStyle { blurRadius(radius) } }

    Row(
        modifier =
            modifier
                .then(if (glassEnabled && !collapsed) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
                .windowInsetsPadding(ShortNavigationBarDefaults.windowInsets)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.animateContentSize().then(if (glassEnabled && !collapsed) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()).height(FloatingBarHeight).then(
                if (glassState != null) Modifier.clip(glassShape).hazeBlur(HazeInput.Backdrop(glassState), glassStyle) else Modifier,
            ),
            shape = if (glassEnabled) glassShape else CircleShape,
            color = if (glassEnabled) glassTint else MaterialTheme.colorScheme.surfaceContainer,
            border = if (glassEnabled) BorderStroke(1.dp, Brush.verticalGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)))) else null,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = FloatingBarShadow,
        ) {
            Row(
                modifier = (if (glassEnabled && !collapsed) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()).padding(horizontal = 8.dp),
                horizontalArrangement = if (glassEnabled) Arrangement.SpaceEvenly else Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (collapsed) {
                    val selectedItem = items.firstOrNull { it.key == selectedKey }
                    if (selectedItem != null) {
                        Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = expand), contentAlignment = Alignment.Center) {
                            Icon(selectedItem.selectedIcon, contentDescription = selectedItem.label, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                } else {
                    pillItems.forEach { item ->
                        if (glassEnabled) {
                            val selected = item.key == selectedKey
                            Box(
                                modifier = itemModifier(item.key).weight(1f).height(FloatingItemHeight)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f) else Color.Transparent)
                                    .selectable(selected = selected, onClick = { onSelect(item.key) }, role = Role.Tab),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(if (selected) item.selectedIcon else item.icon, contentDescription = item.label, tint = MaterialTheme.colorScheme.onSurface)
                                    if (settings.glassNavbarLabels) {
                                        Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        } else {
                            FloatingNavigationItem(
                                item = item,
                                selected = item.key == selectedKey,
                                onClick = { onSelect(item.key) },
                                modifier = itemModifier(item.key),
                            )
                        }
                    }
                }
            }
        }

        if (separateItem != null) {
            val selected = separateItem.key == selectedKey
            val container by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                label = "floatingSearchContainer",
            )
            val content by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                label = "floatingSearchContent",
            )
            Surface(
                onClick = { onSelect(separateItem.key) },
                modifier = Modifier.size(FloatingBarHeight).then(itemModifier(separateItem.key)),
                shape = CircleShape,
                color = container,
                contentColor = content,
                shadowElevation = FloatingBarShadow,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (selected) separateItem.selectedIcon else separateItem.icon,
                        contentDescription = separateItem.label,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> FloatingNavigationItem(
    item: AdaptiveNavigationItem<T>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val indicator by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f),
        label = "floatingItemIndicator",
    )
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "floatingItemContent",
    )
    Row(
        modifier =
            modifier
                .height(FloatingItemHeight)
                .clip(CircleShape)
                .background(indicator)
                .focusOutline(CircleShape)
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                ).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(spring(dampingRatio = 0.9f, stiffness = 700f)),
            exit = fadeOut() + shrinkHorizontally(spring(dampingRatio = 0.9f, stiffness = 700f)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = item.selectedIcon, contentDescription = null, tint = contentColor)
                Spacer(Modifier.width(8.dp))
            }
        }
        Text(
            text = item.label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
