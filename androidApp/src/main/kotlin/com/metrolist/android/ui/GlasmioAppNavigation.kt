package com.metrolist.android.ui

import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.recognize_nearby_music
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.navigation.AdaptiveNavigationItem
import com.metrolist.shared.ui.navigation.LocalExpandGlassNavigation
import com.metrolist.shared.ui.navigation.LocalGlassNavigationMorphProgress
import com.metrolist.shared.ui.navigation.Screen
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.glass.GlasmioBackdropProvider
import io.github.neoruaa.glasmio.glass.render.backdrops.LayerBackdrop
import io.github.neoruaa.glasmio.icons.GlasmioIconProvider
import io.github.neoruaa.glasmio.icons.GlasmioIconResolver
import io.github.neoruaa.glasmio.icons.SfSymbol
import io.github.neoruaa.glasmio.ui.GlasmioNavigationBar
import io.github.neoruaa.glasmio.ui.GlasmioTabItem
import org.jetbrains.compose.resources.stringResource

private fun screenSymbol(screen: Screen): SfSymbol =
    when (screen) {
        Screen.Home -> SfSymbol.House
        Screen.Library -> SfSymbol.MusicNoteList
        Screen.Search -> SfSymbol.Search
        Screen.Profile -> SfSymbol.PersonFilled
        else -> SfSymbol.Sparkles
    }

/** The official Glasmio bar renders host-owned icons; it never loads a symbol font. */
@RequiresApi(31)
@Composable
fun GlasmioAppNavigation(
    items: List<AdaptiveNavigationItem<Screen>>,
    selected: Screen?,
    onSelect: (Screen) -> Unit,
    visible: Boolean,
    backdrop: LayerBackdrop,
    onSearchLongClick: () -> Unit,
    onSearchLongRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val active = selected?.takeIf { key -> items.any { it.key == key } } ?: items.first().key
    val iconMap =
        items.associate { item ->
            screenSymbol(item.key).systemName to if (item.key == active) item.selectedIcon else item.icon
        }
    val resolver = remember(iconMap) { GlasmioIconResolver { name -> iconMap[name] } }
    val tabs =
        items.map { item ->
            GlasmioTabItem(
                key = item.key,
                label = if (Settings.shared.glassNavbarLabels) item.label else "",
                symbol = screenSymbol(item.key),
                contentDescription = item.label,
            )
        }
    val compactProgress = rememberUpdatedState(LocalGlassNavigationMorphProgress.current)
    val expand = LocalExpandGlassNavigation.current
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val isDark = scheme.surface.luminance() < 0.5f
    val recognizeLabel = stringResource(Res.string.recognize_nearby_music)
    var searchBounds by remember { mutableStateOf<Rect?>(null) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentLongClick by rememberUpdatedState(onSearchLongClick)
    val currentLongRelease by rememberUpdatedState(onSearchLongRelease)
    AnimatedVisibility(visible = visible, modifier = modifier) {
        GlasmioTheme(isDark = isDark, colorScheme = scheme, typography = typography) {
            GlasmioIconProvider(resolver = resolver) {
                GlasmioBackdropProvider(backdrop) {
                    GlasmioNavigationBar(
                        items = tabs,
                        selectedKey = active,
                        onSelected = onSelect,
                        onExpand = expand,
                        compactProgress = compactProgress,
                        compactSize = 56.dp,
                        backdrop = backdrop,
                        onItemBoundsChanged = { key, bounds -> if (key == Screen.Search) searchBounds = bounds },
                        modifier =
                            Modifier
                                .windowInsetsPadding(ShortNavigationBarDefaults.windowInsets)
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                                .onGloballyPositioned { coordinates = it }
                                .semantics {
                                    if (Settings.shared.voiceSearchEnabled && items.any { it.key == Screen.Search }) {
                                        customActions =
                                            listOf(
                                                CustomAccessibilityAction(recognizeLabel) {
                                                    currentLongClick()
                                                    currentLongRelease()
                                                    true
                                                },
                                            )
                                    }
                                }.pointerInput(Settings.shared.voiceSearchEnabled) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        val point = coordinates?.localToWindow(down.position)
                                        if (Settings.shared.voiceSearchEnabled && point != null && searchBounds?.contains(point) == true) {
                                            val released =
                                                withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                                    waitForUpOrCancellation()
                                                    true
                                                }
                                            if (released == null) {
                                                currentLongClick()
                                                waitForUpOrCancellation()
                                                currentLongRelease()
                                            }
                                        }
                                    }
                                },
                    )
                }
            }
        }
    }
}
