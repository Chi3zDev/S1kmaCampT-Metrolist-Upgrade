package com.metrolist.android.glass

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
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
        assumeTrue("PixelCopy requires Android 8", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
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
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun captureCenter(activity: LiquidGlassBackdropTestActivity): Int {
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
        InstrumentationRegistry.getInstrumentation().runOnMainSync { activity.glass.getLocationInWindow(position) }
        val color = bitmap.getPixel(position[0] + activity.glass.width / 2, position[1] + activity.glass.height / 2)
        bitmap.recycle()
        return color
    }
}
