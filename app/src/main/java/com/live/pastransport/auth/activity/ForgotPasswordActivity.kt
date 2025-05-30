package com.live.pastransport.auth.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.live.pastransport.R
import com.live.pastransport.base.FORGOT_PASSWORD
import com.live.pastransport.base.ResourceModel
import com.live.pastransport.databinding.ActivityForgotPasswordBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    private val authViewModel by viewModels<AuthViewModel>()

    lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModelSetupAndResponse()
        binding.apply {
            ivBack.setOnClickListener {
                finish()
                applyFadeTransition()
            }
            btnSubmit.setOnClickListener {
                val email = binding.etEmail.text.toString().lowercase().trim()
                when {
                    email.isEmpty() -> showError(R.string.error_enter_email)
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                        .matches() -> showError(R.string.error_invalid_email)

                    else -> performForgot(email)
                }

            }
        }
    }

    private fun showError(messageResId: Int) {
        Utils.errorAlert(this, getString(messageResId))
    }

    private fun performForgot(email: String) {
        val map = HashMap<String, String>().apply {
            put("email", email)
        }
        authViewModel.makPostApiCall(this, FORGOT_PASSWORD, "", true, map)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (it.status) {
                    StatusType.SUCCESS -> handleSuccessResponse(it)
                    StatusType.ERROR -> Utils.errorAlert(this, it.message)
                    else -> Utils.errorAlert(this, it.message)
                }
            }
        }
    }

    private fun handleSuccessResponse(resourceModel: ResourceModel) {
        if (resourceModel.key == FORGOT_PASSWORD) {
            val result: CommonResponse = fromJson(resourceModel.obj!!)
            if (result.code == 200) {
                Utils.successAlert(this@ForgotPasswordActivity,result.message)
                lifecycleScope.launch {
                    delay(1000)
                    finish()
                    applyFadeTransition()
                }

            }
        }
    }
}
