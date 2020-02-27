package com.example.birthday_notifyer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.facebook.drawee.backends.pipeline.Fresco
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Fresco.initialize(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                applicationContext.getString(R.string.channel),
                applicationContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = applicationContext.getString(R.string.description)
            val notificationManager =
                NotificationManagerCompat.from(this)
            notificationManager.createNotificationChannel(channel)
        }
        val workManager = WorkManager.getInstance(this)
        val request = PeriodicWorkRequest.Builder(
            NotificationWorker::class.java,
            1,
            TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            applicationContext.getString(R.string.channel),
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}
