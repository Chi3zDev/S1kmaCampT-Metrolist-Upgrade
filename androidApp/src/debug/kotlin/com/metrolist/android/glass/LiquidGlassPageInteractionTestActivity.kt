package com.metrolist.android.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.metrolist.android.ui.glass.QweBackdropSource

/** Probes composition-local density and nested scroll across the separate page view. */
class LiquidGlassPageInteractionTestActivity : ComponentActivity() {
    var requestedDensity by mutableFloatStateOf(2f)
    var observedDensity = 0f
    var observedScroll = 0f
    val dispatcher = NestedScrollDispatcher()
    private val connection =
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                observedScroll += consumed.y
                return Offset.Zero
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(requestedDensity)) {
                QweBackdropSource(
                    onViewChanged = {},
                    modifier = Modifier.fillMaxSize(),
                    contentModifier = Modifier.fillMaxSize().nestedScroll(connection),
                ) {
                    val density = LocalDensity.current.density
                    SideEffect { observedDensity = density }
                    Box(Modifier.fillMaxSize().nestedScroll(object : NestedScrollConnection {}, dispatcher))
                }
            }
        }
    }
}
