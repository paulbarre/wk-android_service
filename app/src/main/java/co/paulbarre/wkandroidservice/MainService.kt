package co.paulbarre.wkandroidservice

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.util.*

class MainService : Service() {

    var startCalendar: Calendar? = null

    val elapsedTime: Long
        get() {
            val now = Calendar.getInstance()
            return startCalendar?.let { (now.timeInMillis - it.timeInMillis) / 1000 } ?: 0
        }

    inner class LocalBinder : Binder() {
        val service: MainService
            get() = this@MainService
    }

    companion object {
        const val START_COMMAND = "START"
        const val STOP_COMMAND = "STOP"
    }

    private val mBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        Log.d(">>>", "[MainService] onBind")
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "START" -> start()
                "STOP" -> stop()
                else -> Log.d(">>>", "[MainService] onStartCommand received UNKNOWN")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.d(">>>", "[MainService] onStartCommand received START")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("OKLM Title")
                .setContentText("OKLM msg")
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)

        startCalendar = Calendar.getInstance()
        scheduleAlarm()
    }

    private fun stop() {
        Log.d(">>>", "[MainService] onStartCommand received STOP")
        startCalendar = null
        stopForeground(true)
        stopSelf()
    }

    private fun scheduleAlarm() {
        val seconds = 10

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(this, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + seconds * 1000,
                alarmIntent)
    }
}
