package com.spbalkan.simplychat.chat

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spbalkan.simplychat.App.Companion.CHANNEL_ID
import com.spbalkan.simplychat.MainActivity
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.data.ChatRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CheckMessagesWorker @AssistedInject constructor (
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val chatRepository: ChatRepository
    ) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        return try {
            val hasNewMessages = chatRepository.hasUnreadMessages()
            Log.d(TAG, "doWork: hasNewMessages: $hasNewMessages")

            if (hasNewMessages) {
                createNotification(appContext = this.applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error: ${e.message}")
            Result.retry()
        }
    }

    private fun createNotification(appContext: Context) {
        Log.d(TAG, "createNotification: ")

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val textTitle = "New messages"
        val textContent = ""
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_send_24)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(appContext)) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "createNotification: no permission")
                // TODO: Consider calling ActivityCompat#requestPermissions here
                // to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(CHECK_MESSAGES_NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        const val TAG = "zzCheckMessagesWorker"
        const val CHECK_MESSAGES_NOTIFICATION_ID = 1001
    }
}