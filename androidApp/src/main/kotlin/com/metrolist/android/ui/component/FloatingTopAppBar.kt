/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.android.ui.component

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.glassEdgeBrush
import com.metrolist.shared.ui.navigation.LocalGlassNavigationState
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.core.GlassAppearance
import io.github.neoruaa.glasmio.glass.GlasmioBackdropProvider
import io.github.neoruaa.glasmio.glass.GlassSurface
import io.github.neoruaa.glasmio.glass.NavigationGlassMaterial
import io.github.neoruaa.glasmio.glass.render.backdrops.LayerBackdrop

private val FloatingBarElementSize = 48.dp
private val FloatingBarElementShadow = 4.dp

/** Modifier that makes any 48dp action (such as the account button) look like a floating circle. */
@Composable
fun Modifier.floatingBarCircle(): Modifier =
    this
        .size(FloatingBarElementSize)
        .shadow(FloatingBarElementShadow, CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)

/**
 * Icon button used by the top bar. Renders as a plain [IconButton] in the classic top bar,
 * and as a separate floating circle in the floating top bar.
 */
@Composable
fun TopBarIconButton(
    onClick: () -> Unit,
    floating: Boolean,
    glass: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalGlassNavigationState.current
    val radius =
        (
            Settings.shared.glassBlurStrength.coerceIn(0f, 100f) / 100f *
                when (Settings.shared.glassStyle) {
                    "FROSTED" -> 24f
                    "CLEAR" -> 8f
                    else -> 20f
                }
        ).dp
    val blurStyle = remember(radius) { HazeBlurStyle { blurRadius(radius) } }
    val border =
        if (glass) {
            BorderStroke(
                1.dp,
                glassEdgeBrush(Settings.shared.glassStyle == "CLEAR"),
            )
        } else {
            null
        }
    val tint =
        if (glass) {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                alpha = (Settings.shared.glassTintStrength / 100f * 0.7f).coerceIn(0.2f, 0.85f),
            )
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    if (floating) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier =
                modifier
                    .size(FloatingBarElementSize)
                    .shadow(FloatingBarElementShadow, CircleShape)
                    .then(
                        if (glass &&
                            backdrop != null
                        ) {
                            Modifier.clip(CircleShape).hazeBlur(HazeInput.Backdrop(backdrop), blurStyle)
                        } else {
                            Modifier
                        },
                    ).then(if (border != null) Modifier.border(border, CircleShape) else Modifier),
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = tint,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            content = content,
        )
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            shapes = IconButtonDefaults.shapes(),
            content = content,
        )
    }
}

/**
 * Floating top bar: a rounded title pill followed by separate circular action buttons,
 * all floating above the content.
 */
@Composable
fun FloatingTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    glass: Boolean = false,
    glasmioBackdrop: LayerBackdrop? = null,
    subtitle: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit,
) {
    val backdrop = LocalGlassNavigationState.current
    val radius =
        (
            Settings.shared.glassBlurStrength.coerceIn(0f, 100f) / 100f *
                when (Settings.shared.glassStyle) {
                    "FROSTED" -> 24f
                    "CLEAR" -> 8f
                    else -> 20f
                }
        ).dp
    val blurStyle = remember(radius) { HazeBlurStyle { blurRadius(radius) } }
    val border =
        if (glass) {
            BorderStroke(
                1.dp,
                glassEdgeBrush(Settings.shared.glassStyle == "CLEAR"),
            )
        } else {
            null
        }
    val tint =
        if (glass) {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                alpha = (Settings.shared.glassTintStrength / 100f * 0.7f).coerceIn(0.2f, 0.85f),
            )
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopAppBarDefaults.windowInsets.exclude(WindowInsets.captionBar))
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val titleContent: @Composable () -> Unit = {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title()
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    Row(modifier = Modifier.padding(start = 8.dp)) { subtitle() }
                }
            }
        }
        if (glass && glasmioBackdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scheme = MaterialTheme.colorScheme
            val typography = MaterialTheme.typography
            val material =
                if (Settings.shared.glassStyle == "CLEAR") NavigationGlassMaterial.Clear else NavigationGlassMaterial.RegularSmall
            val frost =
                when (Settings.shared.glassStyle) {
                    "CLEAR" -> 0.75.dp
                    "FROSTED" -> 18.dp
                    else -> 5.dp
                }
            GlasmioTheme(
                isDark = scheme.surface.luminance() < 0.5f,
                colorScheme = scheme,
                typography = typography,
                appearance = GlassAppearance(),
            ) {
                GlasmioBackdropProvider(glasmioBackdrop) {
                    GlassSurface(
                        modifier = Modifier.weight(1f).height(FloatingBarElementSize),
                        backdrop = glasmioBackdrop,
                        shape = CircleShape,
                        material = material,
                        frostRadius = frost,
                        contentAlignment = Alignment.CenterStart,
                    ) { titleContent() }
                }
            }
        } else {
            Surface(
                modifier =
                    Modifier.weight(1f).height(FloatingBarElementSize).then(
                        if (glass && backdrop != null) {
                            Modifier.clip(CircleShape).hazeBlur(HazeInput.Backdrop(backdrop), blurStyle)
                        } else {
                            Modifier
                        },
                    ),
                shape = CircleShape,
                color = tint,
                border = border,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = FloatingBarElementShadow,
            ) { titleContent() }
        }
        actions()
    }
}
