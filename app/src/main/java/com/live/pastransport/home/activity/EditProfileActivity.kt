package com.live.pastransport.home.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.live.pastransport.base.EDIT_PROFILE
import com.live.pastransport.base.IS_NOTIFICATION
import com.live.pastransport.base.MyApplication
import com.live.pastransport.databinding.ActivityEditProfileBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.OtpVerifyResponse
import com.live.pastransport.utils.CameraActivity
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getTextRequestBody
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.prepareFilePart
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class EditProfileActivity : CameraActivity() {
    private var data: OtpVerifyResponse.Body? = null

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedImage: String? = null
    private val authViewModel by viewModels<AuthViewModel>()


    override fun selectedImage(imagePath: String?, code: Int?, sUri: Uri) {
        selectedImage = imagePath

        imagePath?.let {
            Glide.with(this).load(it).into(binding.ivProfile)
        }
    }
    private fun handleIntent() {
        data = intent.getSerializableExtra("data") as? OtpVerifyResponse.Body
        binding.apply {
            ivProfile.loadImageFromServer(this@EditProfileActivity, data?.image)
            etName.setText(data?.firstName)
            etLoginPhoneNum.setText(data?.phone)
            ccp.setCountryForNameCode(data?.countryCode)
            etEmail.setText(data?.email)
        }

    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntent()
        viewModelSetupAndResponse()
        setClickListeners()
    }
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {

                            EDIT_PROFILE -> {
                                val result: OtpVerifyResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    MyApplication.prefs?.apply {

                                        saveString("NAME", result.body.firstName)
                                        saveString("IMAGE", result.body.image.toString())

                                    }

                                    Utils.successAlert(this@EditProfileActivity, result.message)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        finish()
                                        applyFadeTransition()
                                    }, 1200)
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setClickListeners() {
        with(binding) {
            ivBack.setOnClickListener {
                finish()
            }
            btnUpdate.setOnClickListener {
                callEditProfileApi()
            }
            ivImagePicker.setOnClickListener{
                getImage(this@EditProfileActivity, 0, 0)
            }

        }
    }
    private fun callEditProfileApi(

    ) {

        var imageMultipart = selectedImage?.let { File(it) }?.let { prepareFilePart("image", it) }

        val map = HashMap<String, RequestBody>()
        map["firstName"] = binding.etName.text.toString().getTextRequestBody()


        if (selectedImage != null) {
            authViewModel.postWithMultipartApiCall(this@EditProfileActivity, EDIT_PROFILE, "", true,
                imageMultipart, map)
        }else {
            imageMultipart = null

            authViewModel.postWithMultipartApiCall(this@EditProfileActivity, EDIT_PROFILE, "", true,
                imageMultipart, map)
        }
    }
}