package com.example.pomodorotimer

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log



/*
another possible solutons for timer using Intent Service with loop
https://stackoverflow.com/questions/37803167/count-times-in-service-with-worker-thread-in-android*/


/***
 *
 * Server only has one job, countdown (even when app itself is pause)
 * on start, create a timer and keep counting down
 * on each tick, send boardcast back to activity to let it update
 * on destroy, destroy the timer (even if it is not done counting)
 */

class CountDownService : Service() {

    var bi = Intent(COUNTDOWN_BR)

    private lateinit var timer: CountDownTimer

    override fun onCreate() {
        super.onCreate()
        return super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

        timer.cancel()

        bi.putExtra("forceStopped", true)
        sendBroadcast(bi)

        return super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var secToCount: Long = intent?.getLongExtra("toCount", -1) ?: -1
        secToCount++  // +1s to avoid some phone's service finish before our own timer class reaches 0


        try {

            timer = object:CountDownTimer(secToCount*1000, 1000) {  // counting down 1s at a time
                override fun onTick(millisUntilFinished: Long) {

                    var msRemain:Long = millisUntilFinished

                    Log.i("timerapp", msRemain.toString())
                    bi.putExtra("toCount", msRemain)
                    sendBroadcast(bi)
                }

                override fun onFinish() {

                    Log.i("timerapp", "timer finish")
                    bi.putExtra("toCount", -1.toLong())
                    sendBroadcast(bi)
                }
            }

            timer.start()

        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    companion object {
        const val COUNTDOWN_BR = "CountDownService.countdown_br"
    }
}