package com.metrolist.android.glass

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView

/** Test-only host: a separate Compose backdrop and a sibling glass, not the application UI. */
class LiquidGlassBackdropTestActivity : ComponentActivity() {
    var backdropColor by mutableStateOf(Color.Red)
    lateinit var glass: LiquidGlassView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val backdrop =
            ComposeView(this).apply {
                setContent { Box(Modifier.fillMaxSize().background(backdropColor)) }
            }
        root.addView(backdrop, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        // Mask the real backdrop so a transparent or missing glass cannot pass the pixel test.
        root.addView(
            View(this).apply { setBackgroundColor(android.graphics.Color.GREEN) },
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        glass =
            LiquidGlassView(this).apply {
                backdropSource = backdrop
                material = GlassMaterial.REGULAR
                enableDynamicBackground = true
                enableChromaticAberration = false
                enableChromaticDispersion = false
                enableSensorHighlight = false
                useShaderPipeline = false
            }
        root.addView(glass, FrameLayout.LayoutParams(160, 160, android.view.Gravity.CENTER))
        setContentView(root)
    }
}
