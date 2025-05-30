package com.live.pastransport.home.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.viewModels

import com.live.pastransport.R
import com.live.pastransport.base.DELETE_ACCOUNT
import com.live.pastransport.base.IS_NOTIFICATION
import com.live.pastransport.base.LOGOUT
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.base.UPDATE_NOTIFICATION_STATUS
import com.live.pastransport.databinding.DialogLogoutBinding
import com.live.pastransport.databinding.FragmentSettingBinding
import com.live.pastransport.home.activity.CMSActivity
import com.live.pastransport.home.activity.ChangePasswordActivity
import com.live.pastransport.home.activity.ContactSupportActivity
import com.live.pastransport.home.activity.MyCardsActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.responseModel.NotificationStatusResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.applyFadeTransition
import com.live.pastransport.utils.clearPrefrences
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.sessionExpire
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private var isNotification = false
    private val authViewModel by viewModels<AuthViewModel>()

   private lateinit var binding: FragmentSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isNotification = prefs?.getInt(IS_NOTIFICATION) == 1
        notificationOnOff()
        viewModelSetupAndResponse()
        setClickListeners()
        setUserTypeView()
    }
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(viewLifecycleOwner) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            UPDATE_NOTIFICATION_STATUS -> {
                                val result: NotificationStatusResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    Utils.successAlert(requireActivity(), result.message)
                                    result.body?.isNotificationEnabled?.let { it1 -> prefs?.saveInt(IS_NOTIFICATION, it1) }
                                    isNotification = result.body?.isNotificationEnabled == 1
                                    notificationOnOff()
                                }
                            }
                            LOGOUT -> {
                                val result: CommonResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
//                                    Utils.successAlert(this, result.message)
                                    prefs?.clearSharedPreference()
                                    clearPrefrences()
                                    sessionExpire()
                                    applyFadeTransition()
//                                    Handler(Looper.getMainLooper()).postDelayed({
//                                        startActivity(Intent(this, LoginActivity::class.java))
//                                        finishAffinity()
//                                        applyFadeTransition()
//                                    }, 1500)
                                }
                            }
                            DELETE_ACCOUNT -> {
                                val result: CommonResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
//                                    Utils.successAlert(this, result.message)
                                    prefs?.clearSharedPreference()
                                    clearPrefrences()
                                    sessionExpire()
                                    applyFadeTransition()
//                                    Handler(Looper.getMainLooper()).postDelayed({
//                                        startActivity(Intent(this, LoginActivity::class.java))
//                                        finishAffinity()
//                                        applyFadeTransition()
//                                    }, 1500)
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
    private fun logOutApi() {
        val map = HashMap<String, String>()
        authViewModel.makPostApiCall(requireActivity(), LOGOUT, "", true, map)
    }
    private fun deactivateAccountApi() {
        val map = HashMap<String, String>()
        authViewModel.makPostApiCall(requireActivity(), DELETE_ACCOUNT, "", true, map)
    }
    private fun notificationOnOff() {
        if (isNotification){
            binding.ofNotification.setImageResource(R.drawable.icon_on)
        }
        else{
            binding.ofNotification.setImageResource(R.drawable.icon_off)
        }
    }
    private fun setUserTypeView() {
        with(binding){
            if (getPrefrence("userType","") == "driver") {
                rlDeleteAccount.gone()
                rlMyCards.gone()
            }
            else{
                rlDeleteAccount.visible()
                rlMyCards.gone()
            }
        }
    }
    private fun setClickListeners() {
        with(binding){
            onOfNotification.setOnClickListener {
                    isNotification = !isNotification
                    if (!isNotification){
                        updateNotificationApi(false)
                    }
                    else{
                        updateNotificationApi(true)
                    }
                    notificationOnOff()

            }
            rlChangePassword.setOnClickListener {
                startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
            }
            rlContactSupport.setOnClickListener {
                startActivity(Intent(requireContext(), ContactSupportActivity::class.java))
            }
            rlMyCards.setOnClickListener {
                startActivity(Intent(requireContext(), MyCardsActivity::class.java))
            }
            rlLogout.setOnClickListener {
                logoutDialog()
            }
            rlDeleteAccount.setOnClickListener {
                deleteAccountDialog()
            }

            rlTermsConditions.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        CMSActivity::class.java
                    ).putExtra("pageName", "SettingTermsConditions")
                )
            }
            rlPrivacyPolicy.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        CMSActivity::class.java
                    ).putExtra("pageName", "SettingPrivacyPolicy")
                )
            }
            rlAboutUs.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        CMSActivity::class.java
                    ).putExtra("pageName", "SettingAboutUs")
                )
            }
        }
    }
    private fun updateNotificationApi(status: Boolean) {
        val notificationStatus: Int = when (status) {
            true->{ 1 }
            false->{ 0 }
        }
        val map = hashMapOf(
            "status" to notificationStatus.toString()
        )
        authViewModel.makPostApiCall(
            requireActivity(), UPDATE_NOTIFICATION_STATUS, "", true, map)
    }
    private fun deleteAccountDialog() {
        val binding = DialogLogoutBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.getWindow()?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.tvHeading.text = getString(R.string.delete_account)
        binding.tvDialogContent.text =
            getString(R.string.are_you_sure_you_want_to_delete_your_account)
        binding.tvYesLogout.setOnClickListener {
            dialog.dismiss()
            deactivateAccountApi()
        }
        binding.tvNoLogout.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun logoutDialog() {
        val binding = DialogLogoutBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.getWindow()?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvYesLogout.setOnClickListener {
            dialog.dismiss()
            logOutApi()
        }
        binding.tvNoLogout.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
