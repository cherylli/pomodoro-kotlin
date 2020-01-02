package com.example.pomodorotimer

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat.Builder


class ForegroundService : Service() {

    var timerStarted = false
    var bi = Intent(ForegroundService.COUNTDOWN_BR)

    private lateinit var timer: CountDownTimer


    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // Prevent creating multiple timer
        if (timerStarted){
            return START_NOT_STICKY
        }

        try {

            timer = object: CountDownTimer(Integer.MAX_VALUE.toLong(), 1000) {  // counting down 1s at a time
                override fun onTick(millisUntilFinished: Long) {

                    var msRemain:Long = millisUntilFinished

                    //Log.i("timerapp", msRemain.toString())
                    bi.putExtra("toCount", msRemain)
                    sendBroadcast(bi)
                }

                override fun onFinish() {

                    Log.i("timerapp", "timer finish")
                }
            }

            timer.start()
            timerStarted = true

        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }


        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification =
            Builder(this, CHANNEL_ID)
                .setContentTitle("Timer On, Lets Go")
                .setContentText("Hasta la vista, baby!")
                .setSmallIcon(R.drawable.timericon)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(9000, notification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if (timerStarted){
            timer.cancel()
        }

        bi.putExtra("forceStopped", true)
        sendBroadcast(bi)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val COUNTDOWN_BR = "ForegounrdService.countdown_br"

    }
}