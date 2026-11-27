package com.metrolist.android.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val TabTopBarHeight = 64.dp

/** Host-provided clearance measured before the scaffold consumes its window insets. */
val LocalTabContentTopPadding = staticCompositionLocalOf<Dp?> { null }

/** Keep scrollable content below the floating bar even after scrolling. */
fun Modifier.glassScreenViewport(topPadding: Dp): Modifier = fillMaxSize().padding(top = topPadding).clipToBounds()

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
