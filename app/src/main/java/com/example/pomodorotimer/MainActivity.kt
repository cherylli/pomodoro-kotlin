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

    val timer = Timer()


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
            if (timer.isCounting){
                Log.i("timerapp", "ignore duplicate starting request")
                makeToast("Already started")
                return@setOnClickListener
            }


            timer.workTimer = if (timer.workTimer > 0) timer.workTimer else 10  // lazy to type in
            timer.breakTimer = if (timer.breakTimer > 0) timer.breakTimer else 8  // lazy to type in


            startTimer()

        }

        // pause also destroy the service, save the remaining time,  make a new one to continue later
        fab_pause.setOnClickListener{
            Log.i("timerapp", "clicked timer pause")

            if (timer.isCounting){
                makeToast("Pause timer")
                stopService(Intent(this, CountDownService::class.java))
                timer.isCounting = false
                timer.needResume = true
            }else{
                // do nothing if timer is not running, click pause when timer is stopped has no effect
                makeToast("Already pause")
            }

        }

        // stop means cancel the timer
        fab_stop.setOnClickListener{
            Log.i("timerapp", "clicked timer stop(cancel)")
            makeToast("Cancel timer")

            // if it is running, and you clicked cancel, destroy the service
            if (timer.isCounting){
                stopService(Intent(this, CountDownService::class.java))
            }

            // if it is already pause, service is already destroy, you just update here in the activity

            val countDownView: TextView = findViewById(R.id.textView_countdown)
            countDownView.setText("Canceled")
            timer.endTimer()
            timer.workState = WorkState.Work

        }


        // TODO force numeric input
        button_set.setOnClickListener {
            Log.i("timerapp", "clicked set button")
            timer.workTimer = editText_pomodoro.text.toString().toInt()
            timer.breakTimer = editText_break.text.toString().toInt()
            Log.i("SetTimer","workTimer set to ${timer.workTimer}, breakTimer set to ${timer.breakTimer}")
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
        Log.i("timerapp", "on Stop")
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

    // on each tick, update the GUI
    private fun handleCountDown(intent: Intent) {

        val countDownView: TextView = findViewById(R.id.textView_countdown)


        // do not react if timer is force stopped
        if (intent.hasExtra("toCount") && !intent.hasExtra("forceStopped")){

            countDownView.setText(timer.displayTime())

            timer.minusOneSecond()

            if (!timer.isCounting){

                stopService(Intent(this, CountDownService::class.java))

                // switch workstate when timer is finish
                when (timer.workState) {
                    WorkState.Work -> {

                        timer.workState = WorkState.Break
                        sendNotification()
                        startTimer()
                    }
                    WorkState.Break -> {

                        timer.workState = WorkState.Work
                        startTimer()
                    }
                }
            }
        }
    }

    private fun startTimer(){

        val startCountDownIntent = Intent(this, CountDownService::class.java)

        when(timer.workState){

            WorkState.Break-> {

                if (!timer.needResume){
                    timer.loadbreakTimer()
                }
                textView_countdown.setTextColor(resources.getColor(R.color.colorBreak))
            }
            WorkState.Work->{

                if (!timer.needResume){
                    timer.loadworkTimer()
                    Log.i("timerapp", "start a new timer with  ${timer.displayTime()}")
                }else{
                    Log.i("timerapp", "resume timer from  ${timer.displayTime()}")
                }
                textView_countdown.setTextColor(resources.getColor(R.color.colorWork))
            }
        }

        if (timer.toSeconds() < 0){
            makeToast("Invalid time")
            return
        }



        timer.needResume = false
        timer.isCounting = true
        startCountDownIntent.putExtra("toCount", timer.toSeconds())
        startService(startCountDownIntent)
    }


}