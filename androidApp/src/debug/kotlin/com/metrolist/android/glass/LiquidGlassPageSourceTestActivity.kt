package com.metrolist.android.glass

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.metrolist.android.ui.glass.QweBackdropSource
import com.metrolist.android.ui.glass.QweGlassOverlay
import com.metrolist.android.ui.glass.QweGlassStyle

/** Two independent capture layers: a page over wallpaper and chrome over the live page. */
class LiquidGlassPageSourceTestActivity : ComponentActivity() {
    var pageColor by mutableStateOf(Color.Green)
    var page: View? = null
    var outerGlass: View? = null
    var innerGlass: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var pageSource by remember { mutableStateOf<View?>(null) }
            var wallpaperSource by remember { mutableStateOf<View?>(null) }
            Box(Modifier.fillMaxSize()) {
                QweBackdropSource(
                    onViewChanged = {
                        pageSource = it
                        page = it
                    },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        QweBackdropSource(onViewChanged = { wallpaperSource = it }, modifier = Modifier.fillMaxSize()) {
                            Box(Modifier.fillMaxSize().background(Color.Red))
                        }
                        Box(Modifier.fillMaxSize().background(pageColor))
                        QweGlassOverlay(
                            source = wallpaperSource,
                            style = QweGlassStyle.FROSTED,
                            radius = 8.dp,
                            tint = Color.Transparent,
                            dynamic = false,
                            lightMode = false,
                            refractionAllowed = false,
                            modifier = Modifier.align(Alignment.TopStart).size(48.dp),
                            onViewChanged = { innerGlass = it },
                        )
                    }
                }
                QweGlassOverlay(
                    source = pageSource,
                    style = QweGlassStyle.FROSTED,
                    radius = 24.dp,
                    tint = Color.Transparent,
                    dynamic = true,
                    lightMode = false,
                    refractionAllowed = false,
                    modifier = Modifier.align(Alignment.Center).size(160.dp),
                    onViewChanged = { outerGlass = it },
                )
            }
        }
    }
}
