package com.example.pomodorotimer

import android.R
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log



/*
another possible solutons for timer using Intent Service with loop
https://stackoverflow.com/questions/37803167/count-times-in-service-with-worker-thread-in-android*/



class CountDownService : Service() {

    var bi = Intent(COUNTDOWN_BR)

    override fun onCreate() {
        super.onCreate()
        return super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val defaultTime: Long = 60000
        var timeRemain: Long = intent?.getLongExtra("timeRemain", defaultTime) ?: defaultTime


        try {

            object: CountDownTimer(timeRemain, 1000) {  // counting donw 1s at a time
                override fun onTick(millisUntilFinished: Long) {
                    bi.putExtra("timeRemain", millisUntilFinished);
                    Log.i("countdown", millisUntilFinished.toString());
                    sendBroadcast(bi);
                }

                override fun onFinish() {
                    Log.i("countdown", "timer finish")
                    bi.putExtra("timeRemain", -1.toLong());
                    sendBroadcast(bi);
                }
            }.start()


        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }


        return super.onStartCommand(intent, flags, startId);
    }

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    companion object {
        const val COUNTDOWN_BR = "CountDownService.countdown_br"
    }
}

