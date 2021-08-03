package com.example.notestodo.todo

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notestodo.MainActivity
import com.example.notestodo.R
import com.example.notestodo.channelId
import com.example.notestodo.todo.ui.tasks.TasksAdapter
import com.example.notestodo.todo.ui.tasks.TasksFragment

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val intentOpenActivity = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val id: Int = System.currentTimeMillis().toInt()
        val contentIntent = PendingIntent.getActivity(context, id, intentOpenActivity, 0)

        val builder = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_todo_noti)
            .setContentTitle("Task Time!")
            .setContentText(intent?.getStringExtra("notificationTitle"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        with(NotificationManagerCompat.from(context)) {
            val notificationId: Int = System.currentTimeMillis().toInt()
            notify(notificationId, builder.build())
        }
    }
}