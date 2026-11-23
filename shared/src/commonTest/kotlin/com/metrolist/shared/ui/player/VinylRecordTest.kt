package com.metrolist.shared.ui.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VinylRecordTest {
    @Test
    fun armMovesInwardAndClampsInvalidProgress() {
        val start = vinylArmContact(0f)
        val end = vinylArmContact(1f)
        assertTrue(end.x < start.x)
        assertTrue(end.y < start.y)
        assertEquals(start, vinylArmContact(-1f))
        assertEquals(end, vinylArmContact(2f))
        assertEquals(start, vinylArmContact(Float.NaN))
    }
}
