package com.devinfusion.eventiafinal.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.devinfusion.eventiafinal.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this@SplashScreenActivity,LoginActivity::class.java)
            startActivity(i)
            finish()
        },2000)
    }
}