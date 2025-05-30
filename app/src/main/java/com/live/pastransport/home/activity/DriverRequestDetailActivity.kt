package com.live.pastransport.home.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.databinding.ActivityDriverRequestDetailBinding
import com.live.pastransport.databinding.DialogCancelTripBinding
import com.live.pastransport.responseModel.DriverHomeRequestResponse
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.loadImageFromServer
import org.json.JSONArray
import org.json.JSONObject

class DriverRequestDetailActivity : AppCompatActivity(), SocketManager.Observer {

    private lateinit var binding: ActivityDriverRequestDetailBinding
    private var requestData: DriverHomeRequestResponse.Body? = null  // Global declaration


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!socketManager.isConnected()) {
            socketManager.init()
        }
        socketManager.registerObserver(this)

        socketManager.bookingAcceptRejectListener()
        requestData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("data", DriverHomeRequestResponse.Body::class.java)
        } else {
            intent.getSerializableExtra("data") as? DriverHomeRequestResponse.Body
        }
        requestData?.let {
            // Use safely
            binding.apply {
                ivUserImage.loadImageFromServer(this@DriverRequestDetailActivity, it.userId.image)
                tvUserName.text=it.userId.firstName+" "+it.userId.lastName
                tvUserNameRequestTitle.text=it.userId.firstName+" "+it.userId.lastName+" has been sent you a request."
                tvPickUpLocation.text=it.tripStart
                tvDropOffLocation.text=it.tripEnd
                date.text=it.date
                time.text=it.startTime
            }
        }
        setClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unregisterObserver(this)

    }
    private fun setClickListeners() {
        with(binding) {
            ivBack.setOnClickListener {
                finish()
        }
            btnAccept.setOnClickListener {
                acceptRejectSocket("1")
            }
            btnReject.setOnClickListener {
                acceptRejectSocket("4")
//                showDialog()
            }
        }
    }
    private fun acceptRejectSocket(s: String) {
        val jsonObjects = JSONObject()
        jsonObjects.put("status", s)
        jsonObjects.put("bookingId", requestData?._id)
        jsonObjects.put("driverId", requestData?.driverId!!._id)
        Log.d("Socket", jsonObjects.toString())
        socketManager.emitEvent(SocketManager.BOOKING_ACCEPT_REJECT_EMIT, jsonObjects)
    }

    private fun showDialog() {
        val dialogBinding = DialogCancelTripBinding.inflate(layoutInflater)
        val dialog = Dialog(this)  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogBinding.btnSubmit.setOnClickListener {
            acceptRejectSocket("4")

            dialog.dismiss()
            finish()
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onResponseArray(event: String, args: JSONArray) {

    }

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {
            SocketManager.BOOKING_ACCEPT_REJECT_LISTENER -> {
               runOnUiThread {
                    try {
                        if (args != null) {
                            Log.e("onResponse: ", args.toString())

                            val status = args.optInt("status", 0)
                            val message = args.optString("message", "")

                            if (status == 206) {
                                Utils.errorAlert(
                                    this,
                                    "You already have another ongoing ride"
                                )
                            } else {
                                if (message == "Your request has been cancelled..") {
                                    finish()
                                } else {
                                    startActivity(
                                        Intent(
                                            this,
                                            StartTripDetailActivity::class.java
                                        )
                                            .putExtra("bookingId", requestData?._id)
                                    )
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.d(ContentValues.TAG, "onResponse: " + e.message)
                    }
                }
            }

        }


    }


    override fun onError(event: String, vararg args: Array<*>) {

    }

    override fun onBlockError(event: String, args: String) {

    }
}