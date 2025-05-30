package com.live.pastransport.auth.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.live.pastransport.R
import com.live.pastransport.base.AUTH_TOKEN
import com.live.pastransport.base.IS_NOTIFICATION
import com.live.pastransport.base.LOGIN
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.base.ROLE
import com.live.pastransport.base.SESSION
import com.live.pastransport.databinding.ActivityLogInBinding
import com.live.pastransport.home.activity.MainActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.OtpVerifyResponse
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.utils.LocationUpdateUtilityActivity
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.savePrefrence
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LogInActivity : LocationUpdateUtilityActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private var deviceToken = ""
    private var isShowPassword = true

    private lateinit var binding: ActivityLogInBinding
    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs?.saveString("lat",lat.toString())
        prefs?.saveString("lng",lng.toString())
        Log.e( "updatedLatLng: ",lat.toString() )

    }

    override fun onChangedLocation(lat: Double, lng: Double) {
        prefs?.saveString("lat",lat.toString())
        prefs?.saveString("lng",lng.toString())
        Log.e( "updatedLatLng: ",lat.toString() )

        stopLocationUpdates()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLiveLocation(this)
        MyApplication.prefs?.getFirebaseToken()


        viewModelSetupAndResponse()
        setClickListeners()
        passwordShowHide()
        setUserTypeView()
    }

    private fun setUserTypeView() {
        with(binding){
            if (getPrefrence("userType","") == "user") {
                linearLayout2.visible()
            }
            else{
                linearLayout2.gone()
            }
        }
    }
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            LOGIN -> {
                                val result: OtpVerifyResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    result.body?.let {
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
                                    initializeSockets()

                                    lifecycleScope.launch {
                                        delay(100)
                                        navigateToMainScreen()
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
    private fun navigateToMainScreen() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.also {
            startActivity(it)
            applyFadeTransition()
        }
    }
    private fun initializeSockets() {
        socketManager.init()

    }
    private fun setClickListeners() {
        with(binding) {
            ivPassOnOff.setOnClickListener {
                isShowPassword = !isShowPassword
                passwordShowHide()
            }
            tvForgotPassword.setOnClickListener {
                startActivity(Intent(this@LogInActivity, ForgotPasswordActivity::class.java))
            }
            tvSignup.setOnClickListener {
                startActivity(Intent(this@LogInActivity, SignUpActivity::class.java))
            }
            btnLogIn.setOnClickListener {
                deviceToken = MyApplication.prefs?.getFcmToken()!!.toString()

                val email = etEmail.text.toString().trim().lowercase()

                val password = etPassword.text.toString().trim()

                if (email.isEmpty()) {
                    Utils.errorAlert(
                        this@LogInActivity,
                        getString(R.string.please_enter_your_email)
                    )
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Utils.errorAlert(
                        this@LogInActivity,
                        getString(R.string.please_enter_valid_email)
                    )
                    return@setOnClickListener
                }
                if (password.isEmpty()) {
                    Utils.errorAlert(
                        this@LogInActivity,
                        getString(R.string.please_enter_your_password)
                    )
                    return@setOnClickListener
                }
                loginApi(email, password)


            }

        }
    }
    private fun passwordShowHide() {
        binding.apply {
            val cursorPosition = etPassword.selectionStart // Save the current cursor position

            if (isShowPassword) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivPassOnOff.setImageResource(R.drawable.hide)
            } else {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivPassOnOff.setImageResource(R.drawable.visible)
            }

            etPassword.setSelection(cursorPosition) // Restore the cursor position
        }

    }
    private fun loginApi(email: String, password: String) {
        val map = hashMapOf(
            "email" to email,
            "password" to password,
            "deviceType" to "2",
            "deviceToken" to deviceToken,
            "role" to if (getPrefrence("userType","")=="user") "1" else "2"
        )
        authViewModel.makPostApiCall(
            this@LogInActivity, LOGIN, "", true, map
        )
    }
}