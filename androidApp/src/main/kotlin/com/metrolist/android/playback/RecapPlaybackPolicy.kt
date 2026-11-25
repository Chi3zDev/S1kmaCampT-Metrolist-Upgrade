package com.metrolist.android.playback

/** A preview may restore only while every relevant ownership signal still matches. */
internal fun recapMayRestore(
    sameService: Boolean,
    localOnly: Boolean,
    sameQueueSession: Boolean,
    sameQueue: Boolean,
    sameRequest: Boolean,
    sameCommand: Boolean,
    sameSeek: Boolean,
    sameRepeat: Boolean,
): Boolean = sameService && localOnly && sameQueueSession && sameQueue && sameRequest && sameCommand && sameSeek && sameRepeat
