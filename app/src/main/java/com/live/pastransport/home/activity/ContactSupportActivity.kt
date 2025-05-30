package com.live.pastransport.home.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.live.pastransport.R
import com.live.pastransport.base.CONTACT_US
import com.live.pastransport.databinding.ActivityContactSupportBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactSupportActivity : AppCompatActivity() {
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityContactSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityContactSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModelSetupAndResponse()
        setClickListeners()
    }
    private fun contactUsApi(name: String, email: String, countryCode: String, phone: String, description: String) {

        val map = hashMapOf(
            "name" to name,
            "email" to email,
            "countryCode" to countryCode,
            "phone" to phone,
            "message" to description
        )
        authViewModel.makPostApiCall(
            this@ContactSupportActivity, CONTACT_US, "", true, map
        )
    }
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            CONTACT_US -> {
                                val result: CommonResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    Utils.successAlert(this, result.message)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        finish()
                                        applyFadeTransition()
                                    }, 2000)
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

    private fun setClickListeners() {
         with(binding) {
            ivBack.setOnClickListener {
                 finish()
             }
             btnSubmit.setOnClickListener {
                 val name = etName.text.toString().trim()
                 val email = etEmail.text.toString().trim().lowercase()
                 val countryCode = ccp.selectedCountryCodeWithPlus.toString()
                 val phone = etLoginPhoneNum.text.toString().trim()
                 val description = etMessage.text.toString().trim()

                 if (name.isEmpty()) {
                     Utils.errorAlert(this@ContactSupportActivity, getString(R.string.enter_name))
                     return@setOnClickListener
                 }
                 if (email.isEmpty()) {
                     Utils.errorAlert(this@ContactSupportActivity, getString(R.string.please_enter_your_email))
                     return@setOnClickListener
                 }
                 if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                     Utils.errorAlert(this@ContactSupportActivity, getString(R.string.please_enter_valid_email))
                     return@setOnClickListener
                 }

                 if (phone.isEmpty()) {
                     Utils.errorAlert(this@ContactSupportActivity, getString(R.string.enter_phone_number))
                     return@setOnClickListener
                 }

                 if (description.isEmpty()) {
                     Utils.errorAlert(this@ContactSupportActivity, getString(R.string.enter_description))
                     return@setOnClickListener
                 }

                 contactUsApi(name, email, countryCode, phone, description)
             }
         }
    }
}