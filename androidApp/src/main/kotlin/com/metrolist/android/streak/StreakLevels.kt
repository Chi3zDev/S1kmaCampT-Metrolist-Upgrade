package com.metrolist.android.streak

import androidx.compose.ui.graphics.Color
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.streak_level_middle
import com.metrolist.shared.generated.resources.streak_level_glowing
import com.metrolist.shared.generated.resources.streak_level_luminous
import com.metrolist.shared.generated.resources.streak_level_radiant
import com.metrolist.shared.generated.resources.streak_level_start
import com.metrolist.shared.generated.resources.streak_level_top
import com.metrolist.shared.generated.resources.streak_level_turquoise
import org.jetbrains.compose.resources.StringResource

internal data class StreakLevel(val minimumDays: Int, val title: StringResource, val color: Color)

internal val streakLevels = listOf(
    StreakLevel(1, Res.string.streak_level_start, Color(0xFFFFD45C)),
    StreakLevel(3, Res.string.streak_level_glowing, Color(0xFFFF9A42)),
    StreakLevel(7, Res.string.streak_level_middle, Color(0xFFFF613C)),
    StreakLevel(14, Res.string.streak_level_luminous, Color(0xFFE35CBA)),
    StreakLevel(30, Res.string.streak_level_top, Color(0xFF719DFF)),
    StreakLevel(60, Res.string.streak_level_turquoise, Color(0xFF50D8CE)),
    StreakLevel(100, Res.string.streak_level_radiant, Color(0xFFB5FFF5)),
)

internal fun streakLevel(days: Int): StreakLevel = streakLevels.lastOrNull { days >= it.minimumDays } ?: streakLevels.first()
