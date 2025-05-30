package com.live.pastransport.home.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.live.pastransport.base.AUTH_TOKEN
import com.live.pastransport.base.GET_CMS
import com.live.pastransport.base.IS_NOTIFICATION
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.ROLE
import com.live.pastransport.base.SESSION
import com.live.pastransport.databinding.ActivitySafetyAcknowledgementBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CmsResponse
import com.live.pastransport.responseModel.OtpVerifyResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.savePrefrence
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SafetyAcknowledgementActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySafetyAcknowledgementBinding
    private val authViewModel by viewModels<AuthViewModel>()
    private var data: OtpVerifyResponse.Body? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySafetyAcknowledgementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleIntent()
        setupViewModel()
        setupClickListeners()
        fetchCmsContent()
    }



    private fun handleIntent() {
        data = intent.getSerializableExtra("data") as? OtpVerifyResponse.Body
    }

    private fun setupViewModel() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        if (response.key == GET_CMS) {
                            val result: CmsResponse = fromJson(response.obj!!)
                            if (result.code == 200) loadCmsHtml(result.body?.description)
                        }
                    }
                    StatusType.ERROR -> {
                        Utils.errorAlert(this, it.message)
                    }

                    StatusType.LOADING -> {

                    }
                }

            }
        }
    }

    private fun loadCmsHtml(description: String?) {
        val htmlDescription = description ?: ""
        val styledHtml = """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <style>
                    body {
                        background-color: #000000;
                        color: #FFFFFF;
                        font-family: -apple-system, Roboto, "Helvetica Neue", sans-serif;
                        font-size: 16px;
                        line-height: 1.7;
                        padding: 16px;
                    }
                    b, strong {
                        font-weight: bold;
                        color: #FFFFFF;
                    }
                    p {
                        margin-bottom: 16px;
                    }
                    ol, ul {
                        margin-left: 20px;
                        padding-left: 10px;
                    }
                    li {
                        margin-bottom: 10px;
                    }
                </style>
            </head>
            <body>
                $htmlDescription
            </body>
            </html>
        """.trimIndent()

        binding.webViewCms.apply {
            setBackgroundColor(Color.TRANSPARENT)
            loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        }
    }

    private fun fetchCmsContent() {
        val params = hashMapOf("role" to "4")
        authViewModel.makPostApiCall(this, GET_CMS, "", true, params)
    }

    private fun setupClickListeners() {
        binding.apply {
            ivBackCms.setOnClickListener {
                finish()
                applyFadeTransition()
            }

            btnAccept.setOnClickListener {
                if (!customCheckbox.isChecked) {
                    Utils.errorAlert(this@SafetyAcknowledgementActivity, "Please acknowledge and agree to the terms.")
                } else if (!emergencyCheckbox.isChecked) {
                    Utils.errorAlert(this@SafetyAcknowledgementActivity, "Please confirm that in an emergency, you must contact 911.")
                } else if (!transportCheckbox.isChecked) {
                    Utils.errorAlert(this@SafetyAcknowledgementActivity, "Please confirm that you are over 18 or have legal guardian consent for transport.")
                } else if (!parentConsentCheckbox.isChecked) {
                    Utils.errorAlert(this@SafetyAcknowledgementActivity, "Please confirm that minors under 16 require written parental consent on file.")
                } else {
                    saveUserPreferences()
                    navigateToMainScreen()
                }
            }
        }
    }

    private fun saveUserPreferences() {
        data?.let {
            MyApplication.prefs?.apply {
                it.isNotificationEnabled?.let { value -> saveInt(IS_NOTIFICATION, value) }
                saveString("NAME", it.firstName)
                saveString("LAST_NAME", it.lastName ?: "")
                saveString("EMAIL", it.email ?: "")
                saveString("IMAGE", it.image.toString())
                saveString("COUNTRY_CODE", it.countryCode.toString())
                saveString("PHONE_NUMBER", it.phone.toString())
            }

            it.token?.let { token -> savePrefrence(AUTH_TOKEN, token) }
            savePrefrence("UserId", it._id.toString())
            savePrefrence(SESSION, "1")
        }
    }

    private fun navigateToMainScreen() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.also {
            startActivity(it)
            applyFadeTransition()
        }
    }
}
