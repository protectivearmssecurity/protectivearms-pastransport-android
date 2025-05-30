package com.live.pastransport.home.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.live.pastransport.R
import com.live.pastransport.base.DRIVER_DETAIL
import com.live.pastransport.databinding.ActivityDriverDetailBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.DriverDetailsResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.loadImageFromServerLicense
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverDetailActivity : AppCompatActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    var driverId = ""
    var id = ""
    lateinit var binding: ActivityDriverDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        driverId = intent.getStringExtra("driverId").toString()
        id = "/$driverId"
        viewModelSetupAndResponse()
        driverDetailApi()
        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            DRIVER_DETAIL + id -> {
                                val result: DriverDetailsResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    binding.apply {
                                        civProfile.loadImageFromServer(
                                            this@DriverDetailActivity,
                                            result.body[0].image
                                        )
                                        rivDrivingLicense.loadImageFromServerLicense(
                                            this@DriverDetailActivity,
                                            result.body[0].drivingLicense
                                        )
                                        rivCertificate.loadImageFromServerLicense(
                                            this@DriverDetailActivity,
                                            result.body[0].armedCertificates
                                        )
                                        tvNameValue.text = result.body[0].firstName
                                        tvPhoneValue.text =
                                            result.body[0].countryCode + " " + result.body[0].phone
                                        tvCarModelType.text = result.body[0].carModel
                                        tvLocation.text = result.body[0].address

                                        if (result.body[0].driverType == 0) {
                                            tvArmedType.text = getString(R.string.un_armed)
                                        } else {
                                            tvArmedType.text = getString(R.string.armed)
                                        }

                                    }

                                }
                            }
                        }
                    }

                    StatusType.ERROR -> {
                        Utils.errorAlert(this, it.message)
                    }

                    else -> {
                        Utils.errorAlert(this, it.message)
                    }
                }
            }
        }
    }

    private fun driverDetailApi() {
        authViewModel.makeGetApiCall(this, DRIVER_DETAIL + id, "", true)
    }
}