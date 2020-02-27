package com.example.birthday_notifyer

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.birthday_notifyer.database.BirthdayDatabase
import com.example.birthday_notifyer.database.PersonBirthday
import kotlinx.coroutines.*
import java.util.*

class NotificationWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {
    private var notificationJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + notificationJob)
    private var people: List<PersonBirthday>? = null

    override fun doWork(): Result {
        val curDate: Calendar = Calendar.getInstance()
        val day = curDate[Calendar.DAY_OF_MONTH]
        val month = curDate[Calendar.MONTH]
        val database: BirthdayDatabase = BirthdayDatabase.getInstance(applicationContext)
        try {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    sendNotif(day, month, database)
                }
            }
        }
        catch (e: Exception) {
            return Result.failure()
        }
        return Result.success()
    }

    private fun sendNotif(day: Int, month: Int, database: BirthdayDatabase){
        people = database.birthdayDatabaseDao.getPeopleList()
        var i = 1
        for (person in people!!) {
            val personBirthday: Calendar = Calendar.getInstance()
            personBirthday.timeInMillis = person.birthdayDate!!
            if (month == personBirthday.get(Calendar.MONTH) && day == personBirthday.get(
                    Calendar.DAY_OF_MONTH
                )
            ) {
                val context = applicationContext
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + person.phoneNum)
                val pendingIntent = getPendingIntent(intent)
                val notification = createNotif(pendingIntent, person)
                val notificationManager =
                    NotificationManagerCompat.from(context)
                notificationManager.notify(i, notification)
                i++
            }
        }
    }

    private fun getPendingIntent(intent: Intent) : PendingIntent{
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    private fun createNotif(pendingIntent: PendingIntent, person:PersonBirthday): Notification {

        return NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.channel)
        )
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(applicationContext.getString(R.string.birthday) + " " + person.name)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .addAction(
                android.R.drawable.ic_menu_call,
                applicationContext.getString(R.string.congratulate),
                pendingIntent
            )
            .build()
    }
}

