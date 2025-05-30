package com.live.pastransport.auth.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.live.pastransport.R
import com.live.pastransport.databinding.ActivityChooseUserTypeBinding
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.savePrefrence

class ChooseUserTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseUserTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseUserTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListeners()
    }

    private fun setClickListeners() {
        with(binding) {
            btnUser.setOnClickListener {
               savePrefrence("userType", "user")
                startActivity(Intent(this@ChooseUserTypeActivity, LogInActivity::class.java))
                applyFadeTransition()
//                finish()
            }
            btnDriver.setOnClickListener {
                savePrefrence("userType", "driver")
                startActivity(Intent(this@ChooseUserTypeActivity, LogInActivity::class.java))
                applyFadeTransition()
//                finish()
            }
        }
    }
}