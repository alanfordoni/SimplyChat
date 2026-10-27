package com.spbalkan.simplychat

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.spbalkan.simplychat.auth.AuthRepository
import com.spbalkan.simplychat.chat.CheckMessagesWorker
import com.spbalkan.simplychat.chat.CheckMessagesWorker.Companion.TAG
import com.spbalkan.simplychat.utils.Constants
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var authRepository: AuthRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel(applicationContext)
        if (authRepository.isUserSignedIn()) {

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "createNotification: no permission")

                return
            } else {
                setupPeriodicMessageCheck()
            }
        }
    }

    private fun setupPeriodicMessageCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val messageCheckRequest = PeriodicWorkRequestBuilder<CheckMessagesWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.WM_CHECK_MESSAGES,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // Keeps the active schedule across app restarts
            messageCheckRequest
        )
    }

    private fun createNotificationChannel(context: Context) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

    companion object {
        const val CHANNEL_ID = "10001"
    }
}