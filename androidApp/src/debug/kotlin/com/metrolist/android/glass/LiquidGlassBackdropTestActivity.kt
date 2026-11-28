package com.metrolist.android.glass

import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.metrolist.android.ui.glass.QweBackdropSource
import com.metrolist.android.ui.glass.QweGlassOverlay
import com.metrolist.android.ui.glass.QweGlassStyle
import com.metrolist.android.ui.glass.QweThemeGlassBackdrop

/** Test-only host: a separate Compose backdrop and glass, not the application UI. */
class LiquidGlassBackdropTestActivity : ComponentActivity() {
    var backdropColor by mutableStateOf(Color.Red)
    var stripedBackdrop by mutableStateOf(false)
    var splitBackdrop by mutableStateOf(false)
    var staticGlass by mutableStateOf(false)
    var glassOffsetDp by mutableStateOf(0)
    var lightMode by mutableStateOf(false)
    var crossWindowProbe by mutableStateOf(false)
    var foregroundProbeColor by mutableStateOf(Color.Green)
    var dialogWindow: Window? = null
    var glass: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var source by remember { mutableStateOf<View?>(null) }
            val glassSize = with(LocalDensity.current) { 160.toDp() }
            Box(Modifier.fillMaxSize()) {
                QweBackdropSource(
                    onViewChanged = { source = it },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (splitBackdrop) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawRect(color = Color.Red, size = Size(size.width / 2f, size.height))
                            drawRect(
                                color = Color.Blue,
                                topLeft = Offset(size.width / 2f, 0f),
                                size = Size(size.width / 2f, size.height),
                            )
                        }
                    } else if (stripedBackdrop) {
                        Canvas(Modifier.fillMaxSize()) {
                            val stripeWidth = 4f
                            var left = 0f
                            var red = true
                            while (left < size.width) {
                                drawRect(
                                    color = if (red) Color.Red else Color.Blue,
                                    topLeft = Offset(left, 0f),
                                    size = Size(stripeWidth, size.height),
                                )
                                left += stripeWidth
                                red = !red
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxSize().background(backdropColor))
                    }
                }
                // A transparent or missing glass cannot pass by merely showing the backdrop.
                Box(Modifier.fillMaxSize().background(if (crossWindowProbe) foregroundProbeColor else Color.Green))
                if (!crossWindowProbe) {
                    QweGlassOverlay(
                        source = source,
                        style = QweGlassStyle.FROSTED,
                        radius = 24.dp,
                        tint = Color.Transparent,
                        dynamic = !staticGlass,
                        lightMode = lightMode,
                        refractionAllowed = false,
                        modifier = Modifier.align(Alignment.Center).absoluteOffset(x = glassOffsetDp.dp).size(glassSize),
                        sourceRevision = Triple(backdropColor, stripedBackdrop, splitBackdrop),
                        onViewChanged = { glass = it },
                    )
                }
                if (crossWindowProbe && source != null) {
                    Dialog(onDismissRequest = { crossWindowProbe = false }) {
                        val dialogView = LocalView.current
                        SideEffect { dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window }
                        val backdrop = remember(source) { QweThemeGlassBackdrop(checkNotNull(source), Unit) }
                        Box(Modifier.size(glassSize)) {
                            backdrop.Background(radius = 24.dp, modifier = Modifier.matchParentSize())
                        }
                    }
                }
            }
        }
    }
}
