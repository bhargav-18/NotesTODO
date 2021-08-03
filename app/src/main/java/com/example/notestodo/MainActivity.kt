package com.example.notestodo

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.notestodo.todo.AlarmReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavBar: BottomNavigationView

    override fun onResume() {
        super.onResume()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavBar = findViewById(R.id.bottom_nav)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        drawerLayout = findViewById(R.id.drawer_layout)

        val navView = findViewById<NavigationView>(R.id.side_nav_view)

        createNotificationChannel()

        setAlarm()

        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(R.id.tasksFragment, R.id.notesFragment)
        ).setOpenableLayout(drawerLayout)
            .build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavBar.setupWithNavController(navController)
        navView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)

        val headerView: View = navView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.userDisplayName)

        userName.text = "Welcome User"

    }

    private fun setAlarm() {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("notificationTitle", "Come on setup TODO for today")
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        intent.data = (Uri.parse("custom://" + System.currentTimeMillis()))
        alarmManager?.cancel(pendingIntent)

        val alarmStartTime: Calendar = Calendar.getInstance()
        val now: Calendar = Calendar.getInstance()
        alarmStartTime.set(Calendar.HOUR_OF_DAY, 8)
        alarmStartTime.set(Calendar.MINUTE, 0)
        alarmStartTime.set(Calendar.SECOND, 0)
        if (now.after(alarmStartTime)) {
            alarmStartTime.add(Calendar.DATE, 1)
        }

        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmStartTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "Channel for sending task notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (bottomNavBar.selectedItemId == R.id.tasksFragment)
            finishAffinity()
        else
            super.onBackPressed()
    }

}

const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val ADD_NOTE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_NOTE_RESULT_OK = Activity.RESULT_FIRST_USER + 3
const val channelId = "todoChannel"
const val channelName = "TODO Channel"