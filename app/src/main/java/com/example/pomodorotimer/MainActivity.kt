package com.example.pomodorotimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater:MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_timer,menu)
        return true
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_timer -> {
                toolbar.title = "Countdown"
                val countdownFragment  = CountdownFragment.newInstance()
                openFragment(countdownFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_timer_setup -> {
                toolbar.title = "Timer Setup"
                val timerSetupFragment  = TimerSetupFragment.newInstance()
                openFragment(timerSetupFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_settings -> {
                toolbar.title = "Settings"
                val settingsFragment  = SettingsFragment.newInstance()
                openFragment(settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }



    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
