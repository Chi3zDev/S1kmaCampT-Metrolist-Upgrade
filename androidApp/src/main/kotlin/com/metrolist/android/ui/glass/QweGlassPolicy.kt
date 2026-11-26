package com.metrolist.android.ui.glass

/** Keep expensive capture out of the lightweight path; this does not change the user's setting. */
internal fun shouldAttemptQweGlass(
    sdkInt: Int,
    lowRam: Boolean,
    lightMode: Boolean,
    hasSource: Boolean,
): Boolean = sdkInt >= 31 && !lowRam && !lightMode && hasSource
