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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val startCountDownIntent = Intent(this, CountDownService::class.java)
        startCountDownIntent.putExtra("timeRemain", 15000.toLong())
        startService(startCountDownIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_timer,menu)
        return true
    }


    // Receiver receive the background count down info to update the countdown
    private val br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateGUI(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        println("on resume")
        registerReceiver(br, IntentFilter(CountDownService.COUNTDOWN_BR))
    }

    override fun onPause() {
        super.onPause()
        println("on pause")
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        println("killed")
        unregisterReceiver(br)
        stopService(Intent(this, CountDownService::class.java))
        super.onDestroy()
    }


    override fun onBackPressed() {
        println("back button pressed")

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun updateGUI(intent: Intent) {

        if (intent.extras != null) {
            val millisUntilFinished = intent.getLongExtra("timeRemain", 0)

            val countDownView: TextView = findViewById(R.id.textView_countdown)

            if (millisUntilFinished >= 1000){
                countDownView.setText( (millisUntilFinished / 1000).toString())
            }else{
                countDownView.setText("Done")
            }

        }
    }

}
