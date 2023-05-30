package com.example.web_api_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.navigation.findNavController
import com.example.web_api_project.databinding.ActivitySplashBinding
import com.example.web_api_project.ui.meals.MealsActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.PC)
        Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this,MealsActivity::class.java)
            startActivity(intent)
                finish()
        }, 2000)
    }
}