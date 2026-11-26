package com.metrolist.android.glass

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.example.liquidglass.LiquidGlassView
import com.metrolist.android.ui.glass.QweGlassStyle
import com.metrolist.android.ui.glass.QweGlassView

/** Test-only host: a separate Compose backdrop and a sibling glass, not the application UI. */
class LiquidGlassBackdropTestActivity : ComponentActivity() {
    var backdropColor by mutableStateOf(Color.Red)
    var stripedBackdrop by mutableStateOf(false)
    lateinit var glass: LiquidGlassView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val backdrop =
            ComposeView(this).apply {
                setContent {
                    if (stripedBackdrop) {
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
            }
        root.addView(backdrop, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        // Mask the real backdrop so a transparent or missing glass cannot pass the pixel test.
        root.addView(
            View(this).apply { setBackgroundColor(android.graphics.Color.GREEN) },
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        glass =
            QweGlassView(this).apply {
                configure(
                    source = backdrop,
                    style = QweGlassStyle.FROSTED,
                    radiusPx = 24f,
                    tintArgb = android.graphics.Color.TRANSPARENT,
                    dynamic = true,
                )
                // Exercise the non-AGSL path, including the Android 12 renderer.
                useShaderPipeline = false
            }
        root.addView(glass, FrameLayout.LayoutParams(160, 160, android.view.Gravity.CENTER))
        setContentView(root)
    }
}
