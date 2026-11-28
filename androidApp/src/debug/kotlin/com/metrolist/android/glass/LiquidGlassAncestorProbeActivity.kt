package com.metrolist.android.glass

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView
import com.metrolist.android.ui.glass.QweBackdropSource

/** Debug-only probe: deliberately tests upstream ancestor capture without relaxing production guards. */
class LiquidGlassAncestorProbeActivity : ComponentActivity() {
    var sceneColor by mutableStateOf(Color.Green)
    var stripedScene by mutableStateOf(false)
    var pageSource: View? = null
    var glass: LiquidGlassView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var page by remember { mutableStateOf<View?>(null) }
            QweBackdropSource(
                onViewChanged = {
                    page = it
                    pageSource = it
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(Modifier.fillMaxSize().background(sceneColor)) {
                    if (stripedScene) {
                        Canvas(Modifier.fillMaxSize()) {
                            var left = 0f
                            var red = true
                            while (left < size.width) {
                                drawRect(
                                    color = if (red) Color.Red else Color.Blue,
                                    topLeft = Offset(left, 0f),
                                    size = Size(4f, size.height),
                                )
                                left += 4f
                                red = !red
                            }
                        }
                    }
                    page?.let { source ->
                        AndroidView(
                            modifier = Modifier.align(Alignment.Center).size(160.dp),
                            factory = { context ->
                                LiquidGlassView(context).apply {
                                    isClickable = false
                                    isFocusable = false
                                    enablePressEffect = false
                                    enableSensorHighlight = false
                                    enableAdaptiveTint = false
                                    enableChromaticAberration = false
                                    enableChromaticDispersion = false
                                    useShaderPipeline = false
                                    material = GlassMaterial.REGULAR
                                    blurAmount = 0.4f
                                    cornerRadius = 24f * context.resources.displayMetrics.density
                                    glassTint = android.graphics.Color.TRANSPARENT
                                    backdropSource = source
                                    enableDynamicBackground = true
                                    glass = this
                                }
                            },
                            update = { it.backdropSource = source },
                            onRelease = {
                                it.enableDynamicBackground = false
                                it.backdropSource = null
                                glass = null
                            },
                        )
                    }
                }
            }
        }
    }
}
