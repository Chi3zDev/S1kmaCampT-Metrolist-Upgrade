package com.metrolist.shared.ui.player

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerLayoutModeTest {
    @Test
    fun portraitControlsAdaptWithoutShrinkingOrGrowingUnbounded() {
        assertEquals(64.dp, portraitPrimaryControlSize(320.dp, 480.dp))
        assertEquals(72.dp, portraitPrimaryControlSize(400.dp, 800.dp))
        assertEquals(88.dp, portraitPrimaryControlSize(650.dp, 1400.dp))
        assertEquals(64.dp, portraitPrimaryControlSize(300.dp, 1400.dp))
    }
}
