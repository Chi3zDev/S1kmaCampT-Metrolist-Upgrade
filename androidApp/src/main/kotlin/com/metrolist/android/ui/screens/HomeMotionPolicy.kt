package com.metrolist.android.ui.screens

internal fun floatingActionsMotionAllowed(visible: Boolean, lightMode: Boolean, deviceAllowed: Boolean): Boolean =
    visible && !lightMode && deviceAllowed
