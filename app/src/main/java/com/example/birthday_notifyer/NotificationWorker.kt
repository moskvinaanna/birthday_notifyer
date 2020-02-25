package com.example.birthday_notifyer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
                            val pendingIntent = PendingIntent.getActivity(
                                applicationContext,
                                0,
                                intent,
                                PendingIntent.FLAG_CANCEL_CURRENT
                            )

                            val notification =
                                NotificationCompat.Builder(
                                    context,
                                    "periodic_notification_channel_id"
                                )
                                    .setContentTitle("День Рождения!!")
                                    .setContentText("День Рождения у " + person.name)
                                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                    .addAction(
                                        android.R.drawable.ic_menu_call,
                                        "Поздравить",
                                        pendingIntent
                                    )
                                    .build()


                            val notificationManager =
                                NotificationManagerCompat.from(context)
                            notificationManager.notify(i, notification)
                            i++
                        }
                    }
                }
            }
        }
        catch (e: Exception) {
            return Result.failure()
        }
        return Result.success()
    }

    private fun getAllPeople(database: BirthdayDatabase){
        uiScope.launch {
            withContext(Dispatchers.IO){
                people = database.birthdayDatabaseDao.getPeopleList()
            }
        }
    }
}

