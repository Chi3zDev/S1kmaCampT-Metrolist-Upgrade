package com.metrolist.shared.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp

/** Clip the scrolling viewport below the host's inset and reserved top-bar slot. */
fun Modifier.glassScreenViewport(topPadding: Dp): Modifier = fillMaxSize().padding(top = topPadding).clipToBounds()

/** Reserve a Scaffold's measured bars and insets before clipping its scrolling content. */
fun Modifier.glassScaffoldViewport(paddingValues: PaddingValues): Modifier = fillMaxSize().padding(paddingValues).clipToBounds()

/** Host supplies the inset and bar so their calculation remains correct in each navigation layout. */
@Composable
fun GlassScreenFrame(
    topInset: Dp,
    barHeight: Dp,
    topBar: @Composable () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        content(Modifier.glassScreenViewport(topInset + barHeight))
        Box(Modifier.fillMaxWidth().padding(top = topInset)) { topBar() }
    }
}
