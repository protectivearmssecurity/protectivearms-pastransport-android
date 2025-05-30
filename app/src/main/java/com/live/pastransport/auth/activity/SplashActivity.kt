package com.live.pastransport.auth.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.base.SESSION
import com.live.pastransport.databinding.ActivitySplashBinding
import com.live.pastransport.home.activity.MainActivity
import com.live.pastransport.utils.LocationUpdateUtilityActivity
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.getPrefrence
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : LocationUpdateUtilityActivity() {

    private lateinit var binding: ActivitySplashBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permission = arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLiveLocation(this)

        callNextScreen()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermissions(permission)) {
                requestPermission()
            } else {
                callNextScreen()
            }
        } else {
            callNextScreen()
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
        Log.e( "updatedLatLng: ",lat.toString() )
    }

    override fun onChangedLocation(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
        Log.e( "updatedLatLng: ",lat.toString() )

        stopLocationUpdates()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        requestNotificationPermissions.launch(permission)
    }

    private val requestNotificationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.isNotEmpty()) {
                permissions.entries.forEach {
                    Log.d("permissions", "${it.key} = ${it.value}")
                    callNextScreen()
                }
            } else {
                callNextScreen()
            }
        }


    private fun callNextScreen() {
        lifecycleScope.launch {
            delay(2000)
            if (getPrefrence(SESSION, "") == "1") {
                val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                applyFadeTransition()
            } else {
                val intent = Intent(this@SplashActivity, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                applyFadeTransition()
            }
        }
    }
}
