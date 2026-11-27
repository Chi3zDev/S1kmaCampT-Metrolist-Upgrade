package com.metrolist.android.glass

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import com.metrolist.android.ui.glass.QweGlassStyle
import com.metrolist.android.ui.glass.QweGlassView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import android.graphics.Color as AndroidColor

class LiquidGlassBackdropTest {
    /** Demonstrates that a moving Compose sibling is actually sampled; it does not certify visual quality. */
    @Test
    fun dynamicGlassUpdatesWhenSeparateComposeBackdropChanges() {
        assumeTrue("Glass bridge uses the lightweight fallback below Android 12", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activityManager = instrumentation.targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        assumeTrue("Glass bridge uses the lightweight fallback on low-RAM devices", !activityManager.isLowRamDevice)
        assumeTrue("QWEA0 native library is unavailable for this process ABI", runCatching { System.loadLibrary("nativegauss") }.isSuccess)
        val intent =
            Intent(instrumentation.targetContext, LiquidGlassBackdropTestActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent) as LiquidGlassBackdropTestActivity
        try {
            instrumentation.waitForIdleSync()
            Thread.sleep(1000)
            val red = captureCenter(activity)
            instrumentation.runOnMainSync { activity.backdropColor = Color.Blue }
            instrumentation.waitForIdleSync()
            Thread.sleep(1000)
            val blue = captureCenter(activity)
            assertTrue("red backdrop must reach glass: $red", AndroidColor.red(red) > AndroidColor.blue(red) + 20)
            assertTrue("updated blue backdrop must reach glass: $blue", AndroidColor.blue(blue) > AndroidColor.red(blue) + 20)

            instrumentation.runOnMainSync { activity.stripedBackdrop = true }
            instrumentation.waitForIdleSync()
            Thread.sleep(1000)
            val stripeSamples = captureCenterRow(activity, 32)
            val redValues = stripeSamples.map(AndroidColor::red)
            val blueValues = stripeSamples.map(AndroidColor::blue)
            assertTrue("blurred stripes should contain red", redValues.average() > 25)
            assertTrue("blurred stripes should contain blue", blueValues.average() > 25)
            assertTrue("red stripes should be smoothed by blur: $redValues", redValues.max() - redValues.min() < 130)
            assertTrue("blue stripes should be smoothed by blur: $blueValues", blueValues.max() - blueValues.min() < 130)

            val glass = checkNotNull(activity.glass) as QweGlassView
            assertFalse("refraction can be disabled", glass.useShaderPipeline)
            instrumentation.runOnMainSync {
                val source = checkNotNull(glass.backdropSource)
                assertTrue("visible dynamic glass should redraw", glass.enableDynamicBackground)
                glass.onVisibilityAggregated(false)
                assertFalse("aggregated invisibility should stop redraw", glass.enableDynamicBackground)
                glass.onVisibilityAggregated(true)
                assertTrue("visible glass should resume redraw", glass.enableDynamicBackground)
                glass.configure(
                    source = source,
                    style = QweGlassStyle.CLEAR,
                    radiusPx = 24f,
                    tintArgb = AndroidColor.TRANSPARENT,
                    dynamic = false,
                    blurStrength = 0f,
                )
                assertEquals("Clear enables the lens on Android 13+", Build.VERSION.SDK_INT >= 33, glass.useShaderPipeline)
                assertFalse("zero blur must disable QWE's built-in base blur", glass.enableBackdropBlur)
                glass.configure(
                    source = activity.window.decorView,
                    style = QweGlassStyle.REGULAR,
                    radiusPx = 24f,
                    tintArgb = AndroidColor.TRANSPARENT,
                    dynamic = true,
                )
                assertEquals("an ancestor must not be captured", View.INVISIBLE, glass.visibility)
                assertFalse("unsafe capture must stop dynamic redraw", glass.enableDynamicBackground)
                glass.configure(
                    source = source,
                    style = QweGlassStyle.REGULAR,
                    radiusPx = 24f,
                    tintArgb = AndroidColor.TRANSPARENT,
                    dynamic = true,
                )
                assertEquals("a safe source should restore visibility", View.VISIBLE, glass.visibility)
                assertTrue("a safe visible source should resume redraw", glass.enableDynamicBackground)
                assertFalse("the visual layer must not intercept clicks", glass.isClickable)
                assertFalse("the visual layer must not take focus", glass.isFocusable)
                activity.lightMode = true
            }
            instrumentation.waitForIdleSync()
            Thread.sleep(500)
            instrumentation.runOnMainSync {
                assertNull("light mode should remove the glass", activity.glass)
                assertFalse("light mode should stop dynamic capture", glass.enableDynamicBackground)
                assertFalse("light mode should detach the glass", glass.isAttachedToWindow)
            }
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    /** Lazy rows use static capture; moving one must sample the new screen position. */
    @Test
    fun staticGlassResamplesWhenMovedAcrossBackdrop() {
        assumeTrue("Glass bridge uses the lightweight fallback below Android 12", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activityManager = instrumentation.targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        assumeTrue("Glass bridge uses the lightweight fallback on low-RAM devices", !activityManager.isLowRamDevice)
        assumeTrue("QWEA0 native library is unavailable for this process ABI", runCatching { System.loadLibrary("nativegauss") }.isSuccess)
        val intent =
            Intent(instrumentation.targetContext, LiquidGlassBackdropTestActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent) as LiquidGlassBackdropTestActivity
        try {
            instrumentation.runOnMainSync {
                activity.splitBackdrop = true
                activity.staticGlass = true
                activity.glassOffsetDp = -72
            }
            instrumentation.waitForIdleSync()
            Thread.sleep(1000)
            val left = captureCenter(activity)
            instrumentation.runOnMainSync { activity.glassOffsetDp = 72 }
            instrumentation.waitForIdleSync()
            Thread.sleep(1000)
            val right = captureCenter(activity)
            assertTrue("left sample should be red: $left", AndroidColor.red(left) > AndroidColor.blue(left) + 20)
            assertTrue("moved sample should be blue: $right", AndroidColor.blue(right) > AndroidColor.red(right) + 20)
            instrumentation.runOnMainSync {
                assertFalse(
                    "static glass must not redraw continuously",
                    (checkNotNull(activity.glass) as QweGlassView).enableDynamicBackground,
                )
            }
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun captureCenter(activity: LiquidGlassBackdropTestActivity): Int = captureCenterRow(activity, 1).single()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun captureCenterRow(
        activity: LiquidGlassBackdropTestActivity,
        count: Int,
    ): List<Int> {
        val bitmap = Bitmap.createBitmap(activity.window.decorView.width, activity.window.decorView.height, Bitmap.Config.ARGB_8888)
        val latch = CountDownLatch(1)
        var result = -1
        PixelCopy.request(activity.window, bitmap, { copyResult ->
            result = copyResult
            latch.countDown()
        }, Handler(Looper.getMainLooper()))
        assertTrue("PixelCopy timed out", latch.await(5, TimeUnit.SECONDS))
        assertEquals(PixelCopy.SUCCESS, result)
        val position = IntArray(2)
        var centerX = 0
        var centerY = 0
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val glass = checkNotNull(activity.glass) { "QWEA0 glass view was not created" }
            glass.getLocationInWindow(position)
            centerX = position[0] + glass.width / 2
            centerY = position[1] + glass.height / 2
        }
        val pixels = List(count) { index -> bitmap.getPixel(centerX - count / 2 + index, centerY) }
        bitmap.recycle()
        return pixels
    }
}
