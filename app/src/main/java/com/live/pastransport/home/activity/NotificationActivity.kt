package com.live.pastransport.home.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.live.pastransport.adapter.NotificationAdapter
import com.live.pastransport.base.NOTIFICATIONS_LIST
import com.live.pastransport.databinding.ActivityNotificationBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.NotificationListResponse
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getNotificationApi()
        viewModelSetupAndResponse()
        setClickListener()
    }



    private fun setClickListener() {
        with(binding) {
            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    private fun getNotificationApi() {
        val map=HashMap<String,String>()
        authViewModel.makPostApiCall(this, NOTIFICATIONS_LIST, "",true,map)

    }
    var list=ArrayList<NotificationListResponse.Body.Notification>()
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            NOTIFICATIONS_LIST -> {
                                val result: NotificationListResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    list.clear()
                                    list.addAll(result.body.notifications)
                                    binding.rvNotification.apply {
                                       val  notificationAdapter = NotificationAdapter(this@NotificationActivity,list)
                                        adapter = notificationAdapter
                                    }
                                }
                            }
                        }
                    }

                    StatusType.ERROR -> Utils.errorAlert(this, it.message)

                    else -> Utils.errorAlert(this, it.message)
                }
            }
        }
    }

}