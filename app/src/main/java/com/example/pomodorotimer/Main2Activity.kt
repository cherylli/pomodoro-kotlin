package com.example.pomodorotimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //val bottomNavigation:BottomNavigationView = findViewById(R.id.bottom_navigation)
        //bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        //setSupportActionBar(toolbar)



        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment??:return
        val navController = host.navController


    }
}
