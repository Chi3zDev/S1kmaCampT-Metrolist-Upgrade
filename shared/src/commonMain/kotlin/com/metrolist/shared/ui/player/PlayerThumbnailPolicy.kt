package com.metrolist.shared.ui.player

import com.metrolist.shared.settings.PlayerThumbnailStyle
import com.metrolist.shared.settings.PlayerType

/** Canvas is a selected layout, not an automatic override of Compact or Fullscreen. */
internal fun usesCanvasBackdrop(
    style: PlayerThumbnailStyle,
    type: PlayerType,
): Boolean = style == PlayerThumbnailStyle.CANVAS && type != PlayerType.VINYL

/** Glass Card keeps its own cover layout even if fullscreen thumbnails are selected. */
internal fun usesFullscreenPlayerThumbnail(
    style: PlayerThumbnailStyle,
    type: PlayerType,
): Boolean = style == PlayerThumbnailStyle.FULLSCREEN && type != PlayerType.VINYL && type != PlayerType.GLASS_CARD
