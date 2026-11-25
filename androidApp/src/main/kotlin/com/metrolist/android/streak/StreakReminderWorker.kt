package com.metrolist.android.streak

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.metrolist.android.MainActivity
import com.metrolist.android.R
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.streak_reminder_message
import com.metrolist.shared.generated.resources.streak_reminder_title
import com.metrolist.shared.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString
import org.koin.core.context.GlobalContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** The worker reads the existing qualifying events; it never inserts or changes history. */
class StreakReminderWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val settings = Settings.shared
        if (!settings.streakReminderEnabled) return Result.success()
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val localNow = now.atZone(zone)
        val today = localNow.toLocalDate().toString()
        val minuteNow = localNow.hour * 60 + localNow.minute
        if (settings.streakReminderLastDate == today || minuteNow < settings.streakReminderMinuteOfDay.coerceIn(0, 1439)) {
            return Result.success()
        }
        // A qualifying play is saved at the end of a listening session. Avoid an
        // incorrect reminder while audio is still playing and the event is pending.
        if (applicationContext.getSystemService(AudioManager::class.java)?.isMusicActive == true) return Result.success()
        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return Result.success()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }
        return try {
            val restored = decodeStreakRecovery(settings.streakRecoveryJson) ?: return Result.success()
            val timestamps =
                GlobalContext
                    .get()
                    .get<MusicDatabase>()
                    .dao()
                    .qualifyingStreakTimestamps()
                    .first()
            val streak = calculateListeningStreak(timestamps, now, zone, restored.recoveredDays)
            if (streak.current > 0 && !streak.todayCounted && settings.streakReminderEnabled && settings.streakReminderLastDate != today) {
                manager.createNotificationChannel(
                    NotificationChannelCompat
                        .Builder(CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                        .setName(getString(Res.string.streak_reminder_title))
                        .build(),
                )
                val intent =
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        Intent(applicationContext, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                val notification =
                    NotificationCompat
                        .Builder(applicationContext, CHANNEL)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getString(Res.string.streak_reminder_title))
                        .setContentText(getString(Res.string.streak_reminder_message))
                        .setContentIntent(intent)
                        .setAutoCancel(true)
                        .build()
                // Record before posting: retries must not produce multiple reminders on the same day.
                settings.streakReminderLastDate = today
                settings.flush()
                manager.notify(NOTIFICATION_ID, notification)
            }
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK = "streak-daily-reminder"
        private const val CHANNEL = "streak-reminders"
        private const val NOTIFICATION_ID = 247310

        fun schedule(
            context: Context,
            enabled: Boolean,
            minuteOfDay: Int,
        ) {
            val manager = WorkManager.getInstance(context)
            if (!enabled) {
                manager.cancelUniqueWork(WORK)
                return
            }
            val delay = reminderDelayMillis(Instant.now(), ZoneId.systemDefault(), minuteOfDay)
            val request =
                // Hourly eligibility checks tolerate timezone/DST changes without posting early;
                // the saved local date still limits notifications to one per day.
                PeriodicWorkRequestBuilder<StreakReminderWorker>(1, TimeUnit.HOURS)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()
            manager.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
        }
    }
}

internal fun reminderDelayMillis(
    now: Instant,
    zone: ZoneId,
    minuteOfDay: Int,
): Long {
    val local = now.atZone(zone)
    val time = minuteOfDay.coerceIn(0, 1439)
    var target =
        local
            .toLocalDate()
            .atTime(time / 60, time % 60)
            .atZone(zone)
            .toInstant()
    if (!target.isAfter(now)) {
        target =
            local
                .toLocalDate()
                .plusDays(1)
                .atTime(time / 60, time % 60)
                .atZone(zone)
                .toInstant()
    }
    return Duration.between(now, target).toMillis().coerceAtLeast(0)
}
