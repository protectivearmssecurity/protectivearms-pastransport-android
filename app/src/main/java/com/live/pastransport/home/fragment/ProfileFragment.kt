package com.live.pastransport.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.live.pastransport.base.GET_PROFILE
import com.live.pastransport.databinding.FragmentProfileBinding
import com.live.pastransport.home.activity.DocumentsActivity
import com.live.pastransport.home.activity.EditProfileActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.OtpVerifyResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val authViewModel by viewModels<AuthViewModel>()
    private var data: OtpVerifyResponse.Body? = null

    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners()
        setUserTypeView()
        viewModelSetupAndResponse()
    }

    override fun onResume() {
        super.onResume()
        getProfileApi()
    }
    private fun getProfileApi() {
        val map = HashMap<String, String>()
        authViewModel.makPostApiCall(requireActivity(), GET_PROFILE, "", true, map)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(viewLifecycleOwner) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            GET_PROFILE -> {
                                val result: OtpVerifyResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    binding.apply {
                                         data=result.body
                                        viewPaser.loadImageFromServer(requireContext(), data?.image)
                                        tvName.text=data?.firstName
                                        tvPhoneNumber.text=data?.countryCode+" "+data?.phone
                                        tvEmail.text=data?.email
                                        btnDocuments.setOnClickListener {
                                            startActivity(Intent(requireContext(), DocumentsActivity::class.java)
                                                .putExtra("driverType",data?.driverType.toString())
                                                .putExtra("armedCertificates",data?.armedCertificates)
                                                .putExtra("drivingLicense",data?.drivingLicense)
                                            )
                                        }
                                    }

                                }
                            }
                        }
                    }
                    StatusType.ERROR -> {
                        Utils.errorAlert(requireActivity(), it.message)
                    }
                    else -> {
                        Utils.errorAlert(requireActivity(), it.message)
                    }
                }
            }
        }
    }

    private fun setUserTypeView() {
        with(binding){
            if (getPrefrence("userType","") == "user") {
                btnEditProfile.visible()
                btnDocuments.gone()
            }
            else{
                btnDocuments.visible()
                btnEditProfile.gone()
            }
        }
    }
    private fun setUpClickListeners() {
         with(binding){
             btnEditProfile.setOnClickListener {
                 startActivity(Intent(requireContext(), EditProfileActivity::class.java)
                     .putExtra("data",data)
                 )
             }

         }
    }
}