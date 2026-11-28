package com.metrolist.android.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.exclude
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.ui.components.GlassScreenFrame as SharedGlassScreenFrame
import com.metrolist.shared.ui.components.glassScreenViewport as sharedGlassScreenViewport

val TabTopBarHeight = 64.dp

/** Host-provided clearance measured before the scaffold consumes its window insets. */
val LocalTabContentTopPadding = staticCompositionLocalOf<Dp?> { null }

/** Keep scrollable content below the floating bar even after scrolling. */
fun Modifier.glassScreenViewport(topPadding: Dp): Modifier = sharedGlassScreenViewport(topPadding)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun tabContentTopPadding(): Dp =
    LocalTabContentTopPadding.current
        ?: (
            TopAppBarDefaults.windowInsets
                .exclude(WindowInsets.captionBar)
                .asPaddingValues()
                .calculateTopPadding() + TabTopBarHeight
        )

/** Reserves the status inset and a fixed bar slot, clipping scrolling content below that slot. */
@Composable
fun GlassScreenFrame(
    topInset: Dp,
    barHeight: Dp,
    topBar: @Composable () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    SharedGlassScreenFrame(topInset = topInset, barHeight = barHeight, topBar = topBar, content = content)
}
