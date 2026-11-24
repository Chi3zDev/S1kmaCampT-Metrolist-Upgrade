@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.PolishMotion
import com.metrolist.shared.ui.components.focusOutline
import com.metrolist.shared.ui.haptics.HapticEvent
import com.metrolist.shared.ui.haptics.rememberMetrolistHaptics
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

val LocalFloatingNavBar = compositionLocalOf { false }
val LocalGlassNavbarBottomOffset = compositionLocalOf { 0.dp }
val LocalGlassNavigationEnabled = compositionLocalOf { false }
val LocalGlassNavigationState = compositionLocalOf<HazeState?> { null }
val LocalGlassNavigationMorphProgress = compositionLocalOf { 0f }
val LocalGlassMotionAllowed = compositionLocalOf { true }
val LocalGlassBubbleAllowed = compositionLocalOf { true }
val LocalExpandGlassNavigation = compositionLocalOf<() -> Unit> { {} }

private val FloatingBarHeight = 64.dp
private val FloatingItemHeight = 48.dp
private val FloatingBarShadow = 6.dp
private val NavSpring = PolishMotion.interactiveSpring()

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
    val allowGlassMotion = !LocalGlassNavigationEnabled.current ||
        (LocalGlassMotionAllowed.current && !Settings.shared.glassLightMode && Settings.shared.glassNavbarMotion != "OFF")
    AnimatedVisibility(
        visible = navigationVisible,
        modifier = modifier,
        enter = if (!allowGlassMotion) EnterTransition.None else
            expandVertically(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 700f),
                expandFrom = Alignment.Bottom,
            ),
        exit = if (!allowGlassMotion) ExitTransition.None else
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
    val morphProgress = if (glassEnabled) LocalGlassNavigationMorphProgress.current.coerceIn(0f, 1f) else 0f
    val collapsed = glassEnabled && morphProgress >= 0.5f
    val expand = LocalExpandGlassNavigation.current
    val pillItems = if (glassEnabled) items else items.filter { it.key != separateKey }
    val separateItem = if (glassEnabled) null else items.firstOrNull { it.key == separateKey }
    val settings = Settings.shared
    val motionAllowed = LocalGlassMotionAllowed.current && settings.glassNavbarMotion != "OFF"
    val bubbleEnabled = motionAllowed && LocalGlassBubbleAllowed.current && settings.glassNavbarMotion == "FULL" && !settings.glassLightMode
    val haptics = rememberMetrolistHaptics()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    var rowWidth by remember { mutableIntStateOf(0) }
    var dragX by remember { mutableStateOf<Float?>(null) }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var stretch by remember { mutableStateOf(1f) }
    val currentOnSelect by rememberUpdatedState(onSelect)
    fun itemAt(x: Float): Int {
        if (rowWidth <= 0 || pillItems.isEmpty()) return -1
        val visual = ((x / rowWidth * pillItems.size).toInt()).coerceIn(0, pillItems.lastIndex)
        return if (layoutDirection == LayoutDirection.Rtl) pillItems.lastIndex - visual else visual
    }
    val radius = (settings.glassBlurStrength.coerceIn(0f, 100f) / 100f * when (settings.glassStyle) { "FROSTED" -> 24f; "CLEAR" -> 8f; else -> 20f }).dp
    val tintAlpha = (settings.glassTintStrength.coerceIn(0f, 100f) / 100f * when (settings.glassStyle) { "FROSTED" -> 0.9f; "CLEAR" -> 0.45f; else -> 0.7f }).coerceIn(0.12f, 0.9f)
    val glassTint = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = tintAlpha)
    val cornerPercent = 12f + settings.glassCornerRoundness.coerceIn(0f, 100f) * 0.38f
    val glassShape = remember(cornerPercent, morphProgress) { RoundedCornerShape((cornerPercent + (50f - cornerPercent) * morphProgress).toInt()) }
    val glassStyle = remember(radius) { HazeBlurStyle { blurRadius(radius) } }

    Row(
        modifier =
            modifier
                .then(if (glassEnabled) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
                .windowInsetsPadding(ShortNavigationBarDefaults.windowInsets)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = (if (!glassEnabled) Modifier.animateContentSize() else Modifier).then(if (glassEnabled) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()).height(FloatingBarHeight).then(
                if (glassState != null) Modifier.clip(glassShape).hazeBlur(HazeInput.Backdrop(glassState), glassStyle) else Modifier,
            ),
            shape = if (glassEnabled) glassShape else CircleShape,
            color = if (glassEnabled) glassTint else MaterialTheme.colorScheme.surfaceContainer,
            border = if (glassEnabled) BorderStroke(1.dp, Brush.verticalGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)))) else null,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = FloatingBarShadow,
        ) {
            if (glassEnabled && !collapsed && pillItems.isNotEmpty()) {
                val selectedIndex = pillItems.indexOfFirst { it.key == selectedKey }.coerceAtLeast(0)
                val visualIndex = if (layoutDirection == LayoutDirection.Rtl) pillItems.lastIndex - selectedIndex else selectedIndex
                val targetCenter = if (rowWidth == 0) 0f else (visualIndex + .5f) * rowWidth / pillItems.size
                val animatedCenter = if (bubbleEnabled) {
                    val position = remember { Animatable(targetCenter) }
                    LaunchedEffect(targetCenter, dragX) {
                        val finger = dragX
                        if (finger != null) position.snapTo(finger) else position.animateTo(targetCenter, NavSpring)
                    }
                    dragX ?: position.value
                } else targetCenter
                val cellWidth = if (pillItems.isEmpty()) 0f else rowWidth.toFloat() / pillItems.size
                Box(
                    Modifier.fillMaxWidth().alpha((1f - morphProgress * 2f).coerceIn(0f, 1f)).padding(horizontal = 8.dp)
                        .onSizeChanged { rowWidth = it.width }
                        .pointerInput(pillItems.map { it.key }, rowWidth, layoutDirection) {
                            detectHorizontalDragGestures(
                                onDragStart = { start ->
                                    dragX = start.x.coerceIn(0f, rowWidth.toFloat())
                                    dragIndex = itemAt(start.x)
                                    haptics.perform(HapticEvent.DRAG_START)
                                },
                                onHorizontalDrag = { change, amount ->
                                    change.consume()
                                    val x = ((dragX ?: change.position.x) + amount).coerceIn(0f, rowWidth.toFloat())
                                    dragX = x
                                    stretch = (1f + kotlin.math.abs(amount) / 90f).coerceAtMost(1.18f)
                                    val index = itemAt(x)
                                    if (index != dragIndex) {
                                        dragIndex = index
                                        haptics.perform(HapticEvent.DRAG_SNAP)
                                    }
                                },
                                onDragEnd = {
                                    pillItems.getOrNull(dragIndex)?.let { currentOnSelect(it.key) }
                                    dragX = null; dragIndex = -1; stretch = 1f
                                },
                                onDragCancel = { dragX = null; dragIndex = -1; stretch = 1f },
                            )
                        },
                    contentAlignment = AbsoluteAlignment.CenterLeft,
                ) {
                    if (bubbleEnabled && rowWidth > 0) {
                        val center = dragX ?: animatedCenter
                        val bubbleStretch by animateFloatAsState(if (dragX != null) stretch else 1f, PolishMotion.interactiveSpring(), label = "glassBubbleStretch")
                        val bubbleHeight by animateFloatAsState(if (dragX != null) 0.96f else 1f, PolishMotion.interactiveSpring(), label = "glassBubbleHeight")
                        Box(Modifier.absoluteOffset { IntOffset((center - cellWidth / 2f).toInt(), 0) }
                            .width(with(density) { cellWidth.toDp() }).height(FloatingItemHeight)
                            .graphicsLayer { scaleX = bubbleStretch; scaleY = bubbleHeight }
                            .padding(horizontal = 3.dp).clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)))))
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        pillItems.forEachIndexed { index, item ->
                            val selected = if (dragIndex >= 0) index == dragIndex else item.key == selectedKey
                            Box(
                                modifier = itemModifier(item.key).weight(1f).height(FloatingItemHeight)
                                    .clip(CircleShape)
                                    .background(if (selected && !bubbleEnabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f) else Color.Transparent)
                                    .selectable(selected = item.key == selectedKey, onClick = { onSelect(item.key) }, role = Role.Tab),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(if (selected) item.selectedIcon else item.icon, contentDescription = item.label, tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = if (bubbleEnabled && dragIndex == index) Modifier.graphicsLayer { scaleX = 1.08f; scaleY = 1.08f } else Modifier)
                                    if (settings.glassNavbarLabels) Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = (if (glassEnabled) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()).padding(horizontal = 8.dp),
                    horizontalArrangement = if (glassEnabled) Arrangement.Center else Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (collapsed) {
                        val selectedItem = items.firstOrNull { it.key == selectedKey }
                        if (selectedItem != null) {
                            Box(Modifier.size(48.dp).alpha(((morphProgress - 0.5f) * 2f).coerceIn(0f, 1f)).clip(CircleShape).clickable(onClick = expand), contentAlignment = Alignment.Center) {
                                Icon(selectedItem.selectedIcon, contentDescription = selectedItem.label, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        pillItems.forEach { item ->
                            FloatingNavigationItem(item = item, selected = item.key == selectedKey, onClick = { onSelect(item.key) }, modifier = itemModifier(item.key))
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
