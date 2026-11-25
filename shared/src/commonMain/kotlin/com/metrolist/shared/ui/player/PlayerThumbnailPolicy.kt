package com.metrolist.shared.ui.player

import com.metrolist.shared.settings.PlayerThumbnailStyle
import com.metrolist.shared.settings.PlayerType

/** Canvas is a selected layout, not an automatic override of Compact or Fullscreen. */
internal fun usesCanvasBackdrop(
    style: PlayerThumbnailStyle,
    type: PlayerType,
): Boolean = style == PlayerThumbnailStyle.CANVAS && type != PlayerType.VINYL
