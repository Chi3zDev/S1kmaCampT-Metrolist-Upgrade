package com.metrolist.android.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeMotionPolicyTest {
    @Test
    fun floatingActionsAnimateOnlyWhenAllGatesAllowIt() {
        assertTrue(floatingActionsMotionAllowed(visible = true, lightMode = false, deviceAllowed = true))
        assertFalse(floatingActionsMotionAllowed(visible = false, lightMode = false, deviceAllowed = true))
        assertFalse(floatingActionsMotionAllowed(visible = true, lightMode = true, deviceAllowed = true))
        assertFalse(floatingActionsMotionAllowed(visible = true, lightMode = false, deviceAllowed = false))
    }
}
