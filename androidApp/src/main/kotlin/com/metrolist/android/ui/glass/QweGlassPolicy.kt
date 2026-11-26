package com.metrolist.android.ui.glass

/** Keep expensive capture out of the lightweight path; this does not change the user's setting. */
internal fun shouldAttemptQweGlass(
    sdkInt: Int,
    lowRam: Boolean,
    lightMode: Boolean,
    hasSource: Boolean,
): Boolean = sdkInt >= 31 && !lowRam && !lightMode && hasSource

internal fun qweGlassStyle(style: String): QweGlassStyle =
    when (style) {
        "CLEAR" -> QweGlassStyle.CLEAR
        "FROSTED" -> QweGlassStyle.FROSTED
        else -> QweGlassStyle.REGULAR
    }

/** QWE's default strength is preserved at the app's default slider value of 65. */
internal fun qweBlurAmount(
    style: QweGlassStyle,
    strength: Float,
): Float {
    val safeStrength = if (strength.isFinite()) strength.coerceIn(0f, 100f) else 0f
    val base =
        when (style) {
            QweGlassStyle.CLEAR -> 0.05f
            QweGlassStyle.REGULAR -> 0.18f
            QweGlassStyle.FROSTED -> 0.4f
        }
    return base * safeStrength / 65f
}
