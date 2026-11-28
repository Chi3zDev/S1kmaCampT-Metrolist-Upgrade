package com.metrolist.android.ui.glass

/** Keep sampled content drawable while a player sheet covers it, so glass never samples an empty frame. */
internal fun hideBackdropHost(
    inPictureInPicture: Boolean,
    contentObscured: Boolean,
    hasLiveGlassSource: Boolean,
): Boolean = inPictureInPicture || (contentObscured && !hasLiveGlassSource)

/** Switching an ancestor between offscreen and automatic layers can invalidate a sampled render node mid-transition. */
internal fun cacheSheetContentOffscreen(
    playerProgress: Float,
    settledExpanded: Boolean,
    hasLiveGlassSource: Boolean,
): Boolean = !hasLiveGlassSource && playerProgress > 0f && !settledExpanded
