package com.metrolist.android.streak

import androidx.compose.ui.graphics.Color
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.streak_level_glowing
import com.metrolist.shared.generated.resources.streak_level_luminous
import com.metrolist.shared.generated.resources.streak_level_middle
import com.metrolist.shared.generated.resources.streak_level_radiant
import com.metrolist.shared.generated.resources.streak_level_start
import com.metrolist.shared.generated.resources.streak_level_top
import com.metrolist.shared.generated.resources.streak_level_turquoise
import org.jetbrains.compose.resources.StringResource

internal data class StreakLevel(
    val minimumDays: Int,
    val title: StringResource,
    val color: Color,
    val flameVariant: Int,
)

internal val streakLevels =
    listOf(
        StreakLevel(1, Res.string.streak_level_start, Color(0xFFFFB843), 0),
        StreakLevel(3, Res.string.streak_level_glowing, Color(0xFFFF873D), 1),
        StreakLevel(7, Res.string.streak_level_middle, Color(0xFFFF5449), 2),
        StreakLevel(14, Res.string.streak_level_luminous, Color(0xFFED55A5), 3),
        StreakLevel(30, Res.string.streak_level_top, Color(0xFF8577FF), 4),
        StreakLevel(60, Res.string.streak_level_turquoise, Color(0xFF30CEBE), 5),
        StreakLevel(100, Res.string.streak_level_radiant, Color(0xFF64E8DB), 6),
    )

internal fun streakLevel(days: Int): StreakLevel = streakLevels.lastOrNull { days >= it.minimumDays } ?: streakLevels.first()
