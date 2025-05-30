package com.live.pastransport.auth.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.live.pastransport.R
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.SIGN_UP
import com.live.pastransport.databinding.ActivitySignUpBinding
import com.live.pastransport.home.activity.CMSActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.utils.CameraActivity
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.getTextRequestBody
import com.live.pastransport.utils.prepareFilePart
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class SignUpActivity : CameraActivity() {

    private val authViewModel by viewModels<AuthViewModel>()
    private val listImage = ArrayList<String>()
    private var isShowPassword = true
    private var isShowConfirmPassword = true
    private lateinit var binding: ActivitySignUpBinding
    private var deviceToken = ""

    override fun selectedImage(imagePath: String?, code: Int?, sUri: Uri) {
        imagePath?.let {
            listImage.clear()
            listImage.add(it)
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.profile_placeholder)
                .into(binding.ivProfile)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordToggle()
        observeViewModel()
        setClickListeners()
    }

    private fun setupPasswordToggle() {
        updatePasswordVisibility()
        updateConfirmPasswordVisibility()
    }

    private fun observeViewModel() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (it.status) {
                    StatusType.SUCCESS -> {
                        when (it.key) {
                            SIGN_UP -> {
                                val result: CommonResponse = fromJson(it.obj!!)
                                if (result.code == 200) {
                                    startActivity(
                                        Intent(this, VerificationActivity::class.java).apply {
                                            putExtra("phone", binding.etLoginPhoneNum.text.toString())
                                            putExtra("country_code", binding.ccp.selectedCountryCodeWithPlus.toString())
                                        }
                                    )
                                }
                            }
                        }
                    }

                    StatusType.ERROR->
                        Utils.errorAlert(this, it.message)
                    else -> {
                    Utils.errorAlert(this, it.message)
                }
                }
            }
        }
    }

    private fun handleNextClick() = with(binding) {
        val firstName = etName.text.toString().trim()
        val email = etEmail.text.toString().trim().lowercase()
        val phone = etLoginPhoneNum.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        when {
            listImage.isEmpty() -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_select_profile_pic))
            firstName.isEmpty() -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_enter_name))
            email.isEmpty() -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_enter_email))
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                Utils.errorAlert(this@SignUpActivity, getString(R.string.error_invalid_email))
            phone.isEmpty() -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_enter_phone))
            phone.length < 8 -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_invalid_phone))
            password.isEmpty() -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_enter_password))
            password.length < 6 -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_password_length))
            confirmPassword.isEmpty() -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_confirm_password))
            password != confirmPassword -> Utils.errorAlert(this@SignUpActivity, getString(R.string.error_password_mismatch))
            !customCheckbox.isChecked -> Utils.errorAlert(this@SignUpActivity, getString(R.string.accept_terms))
            else -> signUpApi()
        }
    }

    private fun signUpApi() {
        deviceToken = MyApplication.prefs?.getFcmToken().orEmpty()
        val selectedImage = listImage[0]
        val imageMultipart = File(selectedImage).let { prepareFilePart("image", it) }

        val map = hashMapOf<String, RequestBody>().apply {
            put("email", binding.etEmail.text.toString().lowercase().trim().getTextRequestBody())
            put("firstName", binding.etName.text.toString().trim().getTextRequestBody())
            put("secondName", binding.etName.text.toString().trim().getTextRequestBody()) // same as firstName
            put("countryCode", binding.ccp.selectedCountryCodeWithPlus.toString().trim().getTextRequestBody())
            put("phone", binding.etLoginPhoneNum.text.toString().trim().getTextRequestBody())
            put("password", binding.etPassword.text.toString().trim().getTextRequestBody())
            put("confirm_password", binding.etConfirmPassword.text.toString().trim().getTextRequestBody())
            put("deviceType", "2".getTextRequestBody())
            put("deviceToken", deviceToken.getTextRequestBody())
            put("role", (if (getPrefrence("userType", "") == "user") "1" else "2").getTextRequestBody())
        }

        authViewModel.postWithMultipartApiCall(
            this,
            SIGN_UP,
            "",
            true,
            imageMultipart,
            map
        )
    }

    private fun updatePasswordVisibility() {
        val cursorPosition = binding.etPassword.selectionStart
        binding.etPassword.transformationMethod =
            if (isShowPassword) PasswordTransformationMethod.getInstance()
            else HideReturnsTransformationMethod.getInstance()
        binding.ivPassOnOff.setImageResource(if (isShowPassword) R.drawable.hide else R.drawable.visible)
        binding.etPassword.setSelection(cursorPosition)
    }

    private fun updateConfirmPasswordVisibility() {
        val cursorPosition = binding.etConfirmPassword.selectionStart
        binding.etConfirmPassword.transformationMethod =
            if (isShowConfirmPassword) PasswordTransformationMethod.getInstance()
            else HideReturnsTransformationMethod.getInstance()
        binding.ivPassConfirmOnOff.setImageResource(if (isShowConfirmPassword) R.drawable.hide else R.drawable.visible)
        binding.etConfirmPassword.setSelection(cursorPosition)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setClickListeners() = with(binding) {
        ivPassOnOff.setOnClickListener {
            isShowPassword = !isShowPassword
            updatePasswordVisibility()
        }

        ivPassConfirmOnOff.setOnClickListener {
            isShowConfirmPassword = !isShowConfirmPassword
            updateConfirmPasswordVisibility()
        }

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSubmit.setOnClickListener {
            handleNextClick()
        }

        tvLogin.setOnClickListener {
            finish()
        }

        ivImagePicker.setOnClickListener {
            getImage(this@SignUpActivity, 0, 0)
        }

        tvTermsConditions.setOnClickListener {
            startActivity(
                Intent(this@SignUpActivity, CMSActivity::class.java).putExtra("pageName", "TermsConditions")
            )
        }

        customCheckbox.isChecked = TermsConditions
    }

    override fun onResume() {
        super.onResume()
        binding.customCheckbox.isChecked = TermsConditions
    }

    companion object {
        var TermsConditions = false
    }
}

