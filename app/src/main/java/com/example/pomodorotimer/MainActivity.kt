package com.example.pomodorotimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var counting = false
    var resume = false  // turn to true when you clicked pause
    var toCount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        fab_play.setOnClickListener{

            Log.i("timerapp", "clicked timer start")

            // don't start a new timer if already counting
            if (counting){
                Log.i("timerapp", "ignore duplicate starting request")
                return@setOnClickListener
            }


            if (resume){
                // if resume, use the previous remained time
                Log.i("timerapp", "resume with previous $toCount")
            }else{
                toCount = 20000
                Log.i("timerapp", "start a new timer with  $toCount")
            }

            resume = false
            counting = true

            val startCountDownIntent = Intent(this, CountDownService::class.java)
            startCountDownIntent.putExtra("toCount", toCount)
            startService(startCountDownIntent)

        }

        // pause also destroy the service, save the remaining time,  make a new one to continue later
        fab_pause.setOnClickListener{
            Log.i("timerapp", "clicked timer pause")

            if (counting){
                counting = false
                resume = true
                stopService(Intent(this, CountDownService::class.java))
            }

            // do nothing if timer is not running, click pause when timer is stopped has effect

        }

        // stop means cancel the timer
        fab_stop.setOnClickListener{
            Log.i("timerapp", "clicked timer stop(cancel)")


            // if it is running, and you clicked cancel, destroy the service
            if (counting){
                counting = false
                stopService(Intent(this, CountDownService::class.java))

            // if it is already pause, service is already destroy, you just update here in the activity
            }else{
                resume = false
                handleCancel();
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_timer,menu)
        return true
    }


    // Receiver receive the background count down info to update the countdown
    private val br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleCountDown(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.i("timerapp", "on resume")
        registerReceiver(br, IntentFilter(CountDownService.COUNTDOWN_BR))
    }

    override fun onPause() {
        super.onPause()
        Log.i("timerapp", "on pause")
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        Log.i("timerapp", "on destroy")
        unregisterReceiver(br)
        stopService(Intent(this, CountDownService::class.java))
        super.onDestroy()
    }


    override fun onBackPressed() {
        Log.i("timerapp", "on back button")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun handleCancel(){
        val countDownView: TextView = findViewById(R.id.textView_countdown)
        countDownView.setText("Canceled")
    }

    private fun handleCountDown(intent: Intent) {

        val countDownView: TextView = findViewById(R.id.textView_countdown)

        // if service report timer is force stopped and there is no need to resume, it is a cancel
        if (intent.hasExtra("timerStopped") and intent.getBooleanExtra("timerStopped", false) and !resume){

            handleCancel()

         // normal gui update on each tick
        }else if (intent.hasExtra("toCount")){

            // on each tick, update the GUI save the timeRemain into toCount for pause

            val millisUntilFinished = intent.getLongExtra("toCount", 0)

            if (millisUntilFinished > 1000){
                countDownView.setText( (millisUntilFinished / 1000).toString())
                toCount = millisUntilFinished

            // count down finish
            }else{
                countDownView.setText("Done")
                counting = false
            }

        }
    }

}