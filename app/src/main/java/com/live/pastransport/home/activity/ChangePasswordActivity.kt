package com.live.pastransport.home.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

import com.live.pastransport.R
import com.live.pastransport.base.CHANGE_PASSWORD
import com.live.pastransport.databinding.ActivityChangePasswordBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {
    private var isShowOldPassword = true
    private var isShowNewPassword = true
    private var isShowNewConfirmPassword = true
    private lateinit var binding: ActivityChangePasswordBinding
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModelSetupAndResponse()
        setClickListeners()
    }
    private fun changePasswordApi(oldPassword: String, newPassword: String, confirmNewPassword: String) {
        val map = hashMapOf(
            "old_password" to oldPassword,
            "new_password" to newPassword,
            "confirm_password" to confirmNewPassword
        )
        authViewModel.makPostApiCall(
            this@ChangePasswordActivity, CHANGE_PASSWORD, "", true, map
        )
    }
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            CHANGE_PASSWORD -> {
                                val result: CommonResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    Utils.successAlert(this, result.message)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        finish()
                                        applyFadeTransition()
                                    }, 1000)
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
         with(binding){
             ivBack.setOnClickListener {
                 onBackPressedDispatcher.onBackPressed()
             }
             btnUpdate.setOnClickListener {
                 val oldPassword = etOldPassword.text.toString()
                 val newPassword = etNewPassword.text.toString()
                 val confirmPassword = etConfirmPassword.text.toString()


                     if (oldPassword.isEmpty()) {
                         Utils.errorAlert(
                             this@ChangePasswordActivity,
                             getString(R.string.please_enter_old_password)
                         )
                         return@setOnClickListener
                     }

                 if (newPassword.isEmpty()) {
                     Utils.errorAlert(
                         this@ChangePasswordActivity,
                         getString(R.string.error_enter_password)
                     )
                     return@setOnClickListener
                 }
                 if (confirmPassword.isEmpty()) {
                     Utils.errorAlert(
                         this@ChangePasswordActivity,
                         getString(R.string.please_enter_confirm_password)
                     )
                     return@setOnClickListener
                 }
                 if (newPassword != confirmPassword) {
                     Utils.errorAlert(
                         this@ChangePasswordActivity,
                         getString(R.string.passwords_do_not_match)
                     )
                     return@setOnClickListener
                 }
                     changePasswordApi(etOldPassword.text.toString(),etNewPassword.text.toString(),etConfirmPassword.text.toString())

             }
             ivEye.setOnClickListener {
                 isShowOldPassword = !isShowOldPassword
                 passwordShowHide()
             }
             ivEye2.setOnClickListener {
                 isShowNewPassword = !isShowNewPassword
                 newPasswordShowHide()
             }
             ivEye3.setOnClickListener {
                 isShowNewConfirmPassword = !isShowNewConfirmPassword
                 newConfirmPasswordShowHide()
             }
         }
    }
    private fun passwordShowHide() {
        // Save the current cursor position
        val cursorPosition = binding.etOldPassword.selectionStart
        if (isShowOldPassword) {
            binding.etOldPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivEye.setImageResource(R.drawable.visible)
        } else {
            binding.etOldPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivEye.setImageResource(R.drawable.hide)
        }
        // Restore the cursor position
        binding.etOldPassword.setSelection(cursorPosition)
    }

    private fun newPasswordShowHide() {
        // Save the current cursor position
        val cursorPosition = binding.etNewPassword.selectionStart
        if (isShowNewPassword) {
            binding.etNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivEye2.setImageResource(R.drawable.visible)
        } else {
            binding.etNewPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.ivEye2.setImageResource(R.drawable.hide)
        }
        // Restore the cursor position
        binding.etNewPassword.setSelection(cursorPosition)
    }

    private fun newConfirmPasswordShowHide() {
        // Save the current cursor position
        val cursorPosition = binding.etConfirmPassword.selectionStart
        if (isShowNewConfirmPassword) {
            binding.etConfirmPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            binding.ivEye3.setImageResource(R.drawable.visible)
        } else {
            binding.etConfirmPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.ivEye3.setImageResource(R.drawable.hide)
        }
        // Restore the cursor position
        binding.etConfirmPassword.setSelection(cursorPosition)
    }
}