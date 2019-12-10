package com.example.pomodorotimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    var counting = false
    var resume = false  // turn to true when you clicked pause
    val workTimer: Long = 15000
    val breakTimer: Long = 5000
    var toCount: Long = 0

    private var workState = WorkState.Work //default to start with work timer


    enum class WorkState{
        Work,Break
    }


    val channelId = "pomodoroTimer"
    lateinit var builder: NotificationCompat.Builder


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun sendNotification(){
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(12345, builder.build())
        }
    }

    private fun makeToast(message:String){

        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 200)
        toast.show()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        createNotificationChannel()


        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


         builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.timericon)
            .setContentTitle("Pomodoro Timer")
            .setContentText("Timer complete!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        fab_play.setOnClickListener{

            Log.i("timerapp", "clicked timer start")

            // don't start a new timer if already counting
            if (counting){
                Log.i("timerapp", "ignore duplicate starting request")
                makeToast("Already started")
                return@setOnClickListener
            }
            if (resume){
                // if resume, use the previous remained time
                Log.i("timerapp", "resume with previous $toCount")
                makeToast("Resume timer")
            }else{
                textView_countdown.setTextColor(getResources().getColor(R.color.colorWork))
                makeToast("Starting timer")
                toCount = when (workState) {
                    WorkState.Work -> workTimer
                    else -> breakTimer
                }

                Log.i("timerapp", "start a new timer with  $toCount")
            }
            startTimer(workState)
            resume = false
            counting = true

            //val startCountDownIntent = Intent(this, CountDownService::class.java)
            //startCountDownIntent.putExtra("toCount", toCount)
            //startService(startCountDownIntent)

        }

        // pause also destroy the service, save the remaining time,  make a new one to continue later
        fab_pause.setOnClickListener{
            Log.i("timerapp", "clicked timer pause")

            if (counting){
                makeToast("Pause timer")
                counting = false
                resume = true
                stopService(Intent(this, CountDownService::class.java))
            }else{
                // do nothing if timer is not running, click pause when timer is stopped has effect
                makeToast("Already pause")
            }

        }

        // stop means cancel the timer
        fab_stop.setOnClickListener{
            Log.i("timerapp", "clicked timer stop(cancel)")
            makeToast("Cancel timer")

            // if it is running, and you clicked cancel, destroy the service
            if (counting){
                counting = false
                stopService(Intent(this, CountDownService::class.java))

                // if it is already pause, service is already destroy, you just update here in the activity
            }else{
                resume = false
                handleCancel()
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
                //stopService(Intent(this, CountDownService::class.java))
                when (workState) {
                    WorkState.Work -> {
                        // start the break timer
                        startTimer(WorkState.Break)
                        sendNotification()
                    }
                    WorkState.Break -> {
                        startTimer(WorkState.Work)
                    }
                }
            }
        }
    }

    private fun startTimer(ws:WorkState){
        val startCountDownIntent = Intent(this, CountDownService::class.java)

        //stop the service first if its counting
        if(counting) stopService(Intent(this, CountDownService::class.java))

        when(ws){
            WorkState.Break-> {
                workState = WorkState.Break
                if (!resume and counting){
                    toCount = breakTimer
                }
                textView_countdown.setTextColor(resources.getColor(R.color.colorBreak))
            }
            WorkState.Work->{
                workState = WorkState.Work
                if (!resume and counting){
                    toCount = workTimer
                }
                textView_countdown.setTextColor(resources.getColor(R.color.colorWork))
            }
        }
        startCountDownIntent.putExtra("toCount", toCount)
        startService(startCountDownIntent)
    }


}