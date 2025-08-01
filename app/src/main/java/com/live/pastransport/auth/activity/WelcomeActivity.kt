package com.live.pastransport.auth.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.live.pastransport.R
import com.live.pastransport.adapter.CustomAdapter
import com.live.pastransport.auth.fragment.WelcomeFragment1
import com.live.pastransport.auth.fragment.WelcomeFragment2
import com.live.pastransport.auth.fragment.WelcomeFragment3
import com.live.pastransport.auth.fragment.WelcomeFragment4
import com.live.pastransport.auth.fragment.WelcomeFragment5
import com.live.pastransport.databinding.ActivityWelcomeBinding
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import jp.wasabeef.glide.transformations.BlurTransformation

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    private lateinit var viewPager2: ViewPager2
    private var pageAddress = 1

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Check if the OS version is Android 13 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission is already granted
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                requestNotificationPermission()
            } else {
                // Permission already granted
                // You can proceed with posting notifications
            }
        }
        val pages = listOf(
            Pair(
                "Secure Transport, On-Demand",
                "Request a ride with trained protection specialists, available when and where you need safety most."
            ),
            Pair(
                "Armed & Unarmed Professionals",
                "Every driver is a licensed security officer, equipped to handle high-risk environments and personal protection needs."
            ),
            Pair(
                "Real-Time GPS & Support",
                "Track your ride live and stay connected with our dispatch team. Peace of mind begins the moment you book."
            ),
            Pair(
                "Trusted Transport for Schools & Families",
                "PAS Transport specializes in safe travel for students and families. Background-checked professionals. Always on time."
            ),
            Pair(
                "Corporate & VIP Security Logistics",
                "From executive transport to site-to-site movement, our secure rides are tailored for professionals who can't afford risk."
            )
        )

        viewPager2 = findViewById(R.id.viewPaser)
        val tvNext = findViewById<ImageView>(R.id.btnNext)

        val imageView: ImageView = findViewById(R.id.ivBlur)

        Glide.with(this)
            .load(R.drawable.blur_bg)  // Replace with your image resource or URL
            .transform(BlurTransformation(30))  // Set the blur radius (adjust as needed)
            .into(imageView)

        val fragments: ArrayList<Fragment> = arrayListOf(
            WelcomeFragment1(),
            WelcomeFragment2(),
            WelcomeFragment3(),
            WelcomeFragment4(),
            WelcomeFragment5()
        )
        val adapter = CustomAdapter(fragments, this)
        viewPager2.adapter = adapter

//        dotsIndicator.setViewPager2(viewPager2)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                pageAddress = position + 1
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("Selected_Page", position.toString())

                setIndicator(position)
                val (title, description) = pages[position]
                updateTextWithFade(title, description)


            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        tvNext.setOnClickListener {
            when (pageAddress) {
                1 -> {
                    viewPager2.currentItem = pageAddress
                }

                2 -> {
                    viewPager2.currentItem = pageAddress
                }

                3 -> {
                    viewPager2.currentItem = pageAddress
                }

                4 -> {
                    viewPager2.currentItem = pageAddress
                }

                else -> {
                    Intent(this@WelcomeActivity, ChooseUserTypeActivity::class.java).also {
                        startActivity(it)
                        applyFadeTransition()
                        finish()

                    }
                }
            }
        }

    }

    fun updateTextWithFade(title: String, description: String) {
        binding.tvTitle.animate().alpha(0f).setDuration(150).withEndAction {
            binding.tvTitle.text = title
            binding.tvTitle.animate().alpha(1f).setDuration(150).start()
        }.start()

        binding.tvDescription.animate().alpha(0f).setDuration(150).withEndAction {
            binding.tvDescription.text = description
            binding.tvDescription.animate().alpha(1f).setDuration(150).start()
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now post notifications.
            } else {
                showPermissionRationale()
                // Permission is denied. Handle the case accordingly.
            }
        }

        // Launch the permission dialog
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(this).setTitle("Permission Required")
            .setMessage("This app requires notification permission to notify you about important updates. Please enable the permission in app settings.")
            .setPositiveButton("Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Notification permission is required for the app to function properly.",
                    Toast.LENGTH_SHORT
                ).show()
            }.show()
    }

    private fun setIndicator(position: Int) {
        // Hide all selected and unselected indicators first
        binding.selectedView1.gone()
        binding.selectedView2.gone()
        binding.selectedView3.gone()
        binding.selectedView4.gone()
        binding.selectedView5.gone()
        binding.unSelectedView1.gone()
        binding.unSelectedView2.gone()
        binding.unSelectedView3.gone()
        binding.unSelectedView4.gone()
        binding.unSelectedView5.gone()

        // Show selected and unselected indicators based on position
        when (position) {
            0 -> {
                binding.selectedView1.visible()
                binding.unSelectedView2.visible()
                binding.unSelectedView3.visible()
                binding.unSelectedView4.visible()
                binding.unSelectedView5.visible()
            }

            1 -> {
                binding.selectedView2.visible()
                binding.unSelectedView1.visible()
                binding.unSelectedView3.visible()
                binding.unSelectedView4.visible()
                binding.unSelectedView5.visible()
            }

            2 -> {
                binding.selectedView3.visible()
                binding.unSelectedView1.visible()
                binding.unSelectedView2.visible()
                binding.unSelectedView4.visible()
                binding.unSelectedView5.visible()
            }

            3 -> {
                binding.selectedView4.visible()
                binding.unSelectedView1.visible()
                binding.unSelectedView2.visible()
                binding.unSelectedView3.visible()
                binding.unSelectedView5.visible()
            }

            4 -> {
                binding.selectedView5.visible()
                binding.unSelectedView1.visible()
                binding.unSelectedView2.visible()
                binding.unSelectedView3.visible()
                binding.unSelectedView4.visible()
            }
        }
    }
}
