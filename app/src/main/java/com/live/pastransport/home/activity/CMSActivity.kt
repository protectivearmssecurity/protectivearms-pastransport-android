package com.live.pastransport.home.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.live.pastransport.R
import com.live.pastransport.auth.activity.SignUpActivity.Companion.TermsConditions
import com.live.pastransport.base.GET_CMS
import com.live.pastransport.databinding.ActivityCmsactivityBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CmsResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.gone
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CMSActivity : AppCompatActivity() {
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityCmsactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityCmsactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setClickListeners()
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

    private fun fetchCmsContent(role:String) {
        val params = hashMapOf("role" to role)
        authViewModel.makPostApiCall(this, GET_CMS, "", true, params)
    }
    private fun setClickListeners() {

        binding.ivBackCms.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val pageName = intent.getStringExtra("pageName")
        when (pageName) {
            "TermsConditions" -> {
                fetchCmsContent("2")
                binding.tvHeaderCms.text = getString(R.string.terms_and_conditions)
                binding.btnAccept.setOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                    TermsConditions = true
                }
            }
            "SettingPrivacyPolicy" -> {
                fetchCmsContent("3")
                binding.tvHeaderCms.text = getString(R.string.privacy_policy)
                binding.btnAccept.gone()

            }
            "SettingAboutUs" -> {
                fetchCmsContent("1")
                binding.tvHeaderCms.text = getString(R.string.about_us)
                binding.btnAccept.gone()

            }
            "SettingTermsConditions" -> {
                fetchCmsContent("2")
                binding.tvHeaderCms.text = getString(R.string.terms_and_conditions)
                binding.btnAccept.gone()
            }
        }
    }
}