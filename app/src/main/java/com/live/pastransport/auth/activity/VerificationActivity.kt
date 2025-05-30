package com.live.pastransport.auth.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.live.pastransport.R
import com.live.pastransport.base.RESEND_OTP
import com.live.pastransport.base.ResourceModel
import com.live.pastransport.base.VERIFY_OTP
import com.live.pastransport.databinding.ActivityVerificationBinding
import com.live.pastransport.home.activity.SafetyAcknowledgementActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.responseModel.OtpVerifyResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificationActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var binding: ActivityVerificationBinding

    private var phone: String = ""
    private var countryCode: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            delay(300)
            showStaticOtpAlert()
        }

        phone = intent.getStringExtra("phone").orEmpty()
        countryCode = intent.getStringExtra("country_code").orEmpty()

        setClickListeners()
        observeViewModel()
    }

    private fun showStaticOtpAlert() {
        Utils.successAlert(this, "Please enter static OTP 1111")
    }

    private fun observeViewModel() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> handleSuccessResponse(response)
                    StatusType.ERROR -> Utils.errorAlert(this, response.message)
                    else -> Utils.errorAlert(this, response.message)
                }
            }
        }
    }

    private fun handleSuccessResponse(response: ResourceModel) {
        when (response.key) {
            RESEND_OTP -> {
                val result: CommonResponse = fromJson(response.obj!!)
                if (result.code == 200) {
                    Utils.successAlert(this, "OTP resent successfully")
                    lifecycleScope.launch {
                        delay(300)
                        showStaticOtpAlert()
                    }

                }
            }

            VERIFY_OTP -> {
                val result: OtpVerifyResponse = fromJson(response.obj!!)
                if (result.code == 200) {
                    val data = result.body
                    startActivity(
                        Intent(this, SafetyAcknowledgementActivity::class.java)
                            .putExtra("data", data)
                    )
                    applyFadeTransition()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setClickListeners() {
        binding.apply {
            tvPhoneCode.text = getString(R.string.verification_content) + " $countryCode$phone"

            btnVerify.setOnClickListener {
                val map = hashMapOf(
                    "countryCode" to countryCode,
                    "phone" to phone,
                    "otp" to otpView.text.toString()
                )
                authViewModel.makPostApiCall(this@VerificationActivity, VERIFY_OTP, "", true, map)
            }

            tvResend.setOnClickListener {
                val map = hashMapOf(
                    "countryCode" to countryCode.toString(),
                    "phone" to phone.toString()
                )
                authViewModel.makPostApiCall(this@VerificationActivity, RESEND_OTP, "", true, map)
            }

            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}
