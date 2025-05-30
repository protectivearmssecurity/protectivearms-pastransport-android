package com.live.pastransport.home.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.live.pastransport.R
import com.live.pastransport.databinding.ActivityDocumentsBinding
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.loadImageFromServerLicense

class DocumentsActivity : AppCompatActivity() {
    var driverType=""
    var armedCertificates=""
    var drivingLicense=""
    private lateinit var binding: ActivityDocumentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        driverType=intent.getStringExtra("driverType").toString()
        drivingLicense=intent.getStringExtra("drivingLicense").toString()
        armedCertificates=intent.getStringExtra("armedCertificates").toString()

        binding.apply {
            ivArmedCertificate.loadImageFromServer(this@DocumentsActivity,armedCertificates)
            ivDriving.loadImageFromServerLicense(this@DocumentsActivity,drivingLicense)
            if (driverType == "0") {
                tvCertificateType.text = "Certificates in ${getString(R.string.un_armed)} Training"
            } else {
                tvCertificateType.text = "Certificates in ${getString(R.string.armed)} Training"
            }
        }



        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}