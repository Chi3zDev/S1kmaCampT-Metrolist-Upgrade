package com.metrolist.android.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecapPlaybackPolicyTest {
    @Test
    fun restoresOnlyWhenEveryOwnershipSignalStillMatches() {
        fun evaluate(values: List<Boolean>) =
            recapMayRestore(
                values[0],
                values[1],
                values[2],
                values[3],
                values[4],
                values[5],
                values[6],
                values[7],
            )
        val owned = List(8) { true }
        assertTrue(evaluate(owned))
        for (changed in owned.indices) {
            assertFalse(
                "changed signal $changed must prevent restore",
                evaluate(
                    owned.mapIndexed { i, v ->
                        if (i ==
                            changed
                        ) {
                            false
                        } else {
                            v
                        }
                    },
                ),
            )
        }
    }
}
