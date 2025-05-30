package com.live.pastransport.home.activity

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.live.pastransport.R
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.databinding.ActivityMainBinding
import com.live.pastransport.home.fragment.DriverHomeFragment
import com.live.pastransport.home.fragment.HistoryFragment
import com.live.pastransport.home.fragment.MessageFragment
import com.live.pastransport.home.fragment.ProfileFragment
import com.live.pastransport.home.fragment.SettingFragment
import com.live.pastransport.home.fragment.UserHomeFragment
import com.live.pastransport.utils.LocationUpdateUtilityActivity
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : LocationUpdateUtilityActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private var flightDetails = ""
    private var flightNo = ""
    private var flightArrivalTime = ""
    private var flightGateNo = ""
    private var locationType = ""
    lateinit var binding: ActivityMainBinding


    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
    }

    override fun onChangedLocation(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
        stopLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        locationType = intent.getStringExtra("locationType").toString()
        flightDetails = intent.getStringExtra("flightDetails").toString()
        flightNo = intent.getStringExtra("flightNo").toString()
        flightArrivalTime = intent.getStringExtra("flightArrivalTime").toString()
        flightGateNo = intent.getStringExtra("flightGateNo").toString()

        setContentView(binding.root)
        getLiveLocation(this)

        Log.e("TAG", "onCreate: ${getPrefrence("userType", "")}")
        if (getPrefrence("userType", "") == "user") {
            if (intent.getStringExtra("from") == "nearByDriver") {
                handleTabClick(
                    HistoryFragment(),
                    binding.historySelected,
                    binding.historyUnSelected
                )
                binding.main.backgroundTintList =
                    ContextCompat.getColorStateList(this@MainActivity, R.color.white)
            } else {
                replaceFragment(UserHomeFragment())
            }
        } else {
            if (intent.getStringExtra("from") == "endTrip") {
                handleTabClick(
                    HistoryFragment(),
                    binding.historySelected,
                    binding.historyUnSelected
                )
            } else {
                replaceFragment(DriverHomeFragment())
            }
            binding.main.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.white)
        }
        setupTabClickListeners()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                super.onBackPressed()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun setupTabClickListeners() {
        Log.e("TAG", "onCreate: setupTabClickListeners ${getPrefrence("userType", "")}")
        with(binding) {
            rlHome.setOnClickListener {
                if (getPrefrence("userType", "") == "user") {

                    handleTabClick(
                        UserHomeFragment(),
                        homeSelected,
                        homeUnSelected
                    );main.backgroundTintList =
                        ContextCompat.getColorStateList(this@MainActivity, R.color.black)
                } else {
                    handleTabClick(
                        DriverHomeFragment(),
                        homeSelected,
                        homeUnSelected
                    );main.backgroundTintList =
                        ContextCompat.getColorStateList(this@MainActivity, R.color.white)
                }
            }
            rlHistory.setOnClickListener {
                handleTabClick(
                    HistoryFragment(),
                    historySelected,
                    historyUnSelected
                );main.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.white)
            }
            rlMessage.setOnClickListener {
                handleTabClick(
                    MessageFragment(),
                    messageSelected,
                    messageUnSelected
                ); main.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.black)
            }
            rlProfile.setOnClickListener {
                handleTabClick(
                    ProfileFragment(),
                    profileSelected,
                    profileUnSelected
                ); main.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.black)
            }
            rlSetting.setOnClickListener {
                handleTabClick(
                    SettingFragment(),
                    settingSelected,
                    settingUnSelected
                ); main.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.black)
            }
        }
    }

    open fun handleTabClick(fragment: Fragment, selectedView: View, unselectedView: View) {
        resetTabViews()
        selectedView.visible()
        unselectedView.gone()
        replaceFragment(fragment)
    }

    private fun resetTabViews() {
        val tabViewPairs = listOf(
            binding.rlHome to listOf(binding.homeSelected, binding.homeUnSelected),
            binding.rlHistory to listOf(binding.historySelected, binding.historyUnSelected),
            binding.rlMessage to listOf(binding.messageSelected, binding.messageUnSelected),
            binding.rlProfile to listOf(binding.profileSelected, binding.profileUnSelected),
            binding.rlSetting to listOf(binding.settingSelected, binding.settingUnSelected)
        )

        tabViewPairs.forEach { (tabView, views) ->
            views[0].gone()
            views[1].visible()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("flightDetails", flightDetails)
        bundle.putString("flightNo", flightNo)
        bundle.putString("flightArrivalTime", flightArrivalTime)
        bundle.putString("flightGateNo", flightGateNo)
        bundle.putString("locationType", locationType)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.navhost, fragment)
            .commit()
    }

    companion object {
        var status = 0
    }
}
