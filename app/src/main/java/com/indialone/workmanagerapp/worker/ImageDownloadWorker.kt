package com.indialone.workmanagerapp.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.indialone.workmanagerapp.utils.Constants
import com.indialone.workmanagerapp.utils.getUriFromUrl
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext

class ImageDownloadWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        delay(10000)
        val savedUri = context.getUriFromUrl()
        return Result.success(workDataOf(Constants.IMAGE_URI to savedUri.toString()))
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val intent = WorkManager.getInstance(context)
            .createCancelPendingIntent(id)

        val notification = NotificationCompat
            .Builder(applicationContext, Constants.WORK_DOWNLOAD)
            .setContentTitle("Downloading your Image")
            .setTicker("Downloading your Image")
            .setSmallIcon(R.drawable.notification_action_background)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Cancel Download", intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notification, Constants.WORK_DOWNLOAD)
        }

        return ForegroundInfo(1, notification.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(
        notificationBuilder: NotificationCompat.Builder,
        id: String
    ) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
        val channel = NotificationChannel(
            id,
            Constants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.description = "Work Manager App Notification"
        notificationManager.createNotificationChannel(channel)
    }

}