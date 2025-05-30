package com.live.pastransport.home.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.live.pastransport.adapter.NearByDriverAdapter
import com.live.pastransport.base.BOOK_DRIVER
import com.live.pastransport.base.NEAR_BY_DRIVER
import com.live.pastransport.databinding.ActivityNearbyDriversBinding
import com.live.pastransport.databinding.RequestSendBottomSheetBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.BookDriverResponseModel
import com.live.pastransport.responseModel.NearByDriverResponseModel
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NearbyDriversActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNearbyDriversBinding
    private lateinit var nearbyDriversAdapter: NearByDriverAdapter
    private val authViewModel by viewModels<AuthViewModel>()
    private var driverType = "" //driver 0 for unarmed 1 for armed
    private var locationType = "" //location - 0 for Other , 1 for airport
    private var userDate = ""
    private var startTime = ""
    private var flightDetails = ""
    private var flightNo = ""
    private var flightArrivalTime = ""
    private var flightGateNo = ""
    private var tripStart = ""
    private var tripEnd = ""
    private var startLatitude = ""
    private var startLongitude = ""
    private var endLatitude = ""
    private var endLongitude = ""
    private var driverId = ""
    private var companyName = ""
    private var status = "0"
    private var price = ""
    private var userLatitude = ""
    private var userLongitude = ""
    private var nearByDriverList = mutableListOf<NearByDriverResponseModel.Body?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyDriversBinding.inflate(layoutInflater)
        setContentView(binding.root)

        driverType = intent.getStringExtra("armedType").toString()
        userLatitude = intent.getStringExtra("userLatitude").toString()
        userLongitude = intent.getStringExtra("userLongitude").toString()

        locationType = intent.getStringExtra("locationType").toString()
        userDate = intent.getStringExtra("userDate").toString()
        startTime = intent.getStringExtra("startTime").toString()
        tripStart = intent.getStringExtra("tripStart").toString()
        tripEnd = intent.getStringExtra("tripEnd").toString()
        startLatitude = intent.getStringExtra("startLatitude").toString()
        startLongitude = intent.getStringExtra("startLongitude").toString()
        endLatitude = intent.getStringExtra("endLatitude").toString()
        endLongitude = intent.getStringExtra("endLongitude").toString()
        price = intent.getStringExtra("price").toString()

        flightDetails = if (intent.getStringExtra("flightDetails").toString() == "null") {
            ""
        } else {
            intent.getStringExtra("flightDetails").toString()
        }

        flightNo = if (intent.getStringExtra("flightNo").toString() == "null") {
            ""
        } else {
            intent.getStringExtra("flightNo").toString()
        }

        flightArrivalTime = if (intent.getStringExtra("flightArrivalTime").toString() == "null") {
            ""
        } else {
            intent.getStringExtra("flightArrivalTime").toString()
        }

        flightGateNo = if (intent.getStringExtra("flightGateNo").toString() == "null") {
            ""
        } else {
            intent.getStringExtra("flightGateNo").toString()
        }

//        flightNo = intent.getStringExtra("flightNo").toString()
//        flightArrivalTime = intent.getStringExtra("flightArrivalTime").toString()
//        flightGateNo = intent.getStringExtra("flightGateNo").toString()

        Log.e(
            "TAG",
            "onCreate: $locationType - $userDate - $startTime - $tripStart - $tripEnd - " +
                    "$startLatitude - $startLongitude - $endLatitude - $endLongitude - $price - $flightDetails - " +
                    "$flightNo - $flightArrivalTime - $flightGateNo",
        )

        nearByDriverApi()
        viewModelSetupAndResponse()

//        setUpAdapter()
        setUpListener()
    }

    private fun setUpListener() {
        with(binding) {
            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setUpAdapter() {
        nearbyDriversAdapter = NearByDriverAdapter(nearByDriverList)
        binding.rvNearByDrivers.adapter = nearbyDriversAdapter

        nearbyDriversAdapter.onNearClickListener = { pos,type ->
            driverId = nearByDriverList[pos]?._id.toString()
            companyName = nearByDriverList[pos]?.companyName.toString()
            if (type==0) {
                startActivity(Intent(this,DriverDetailActivity::class.java)
                    .putExtra("driverId",driverId)
                )

            }
            else{
                bookDriverApi()
            }

//            showDialog()
        }
    }

    private fun nearByDriverApi() {
        val map = hashMapOf(
            "latitude" to userLatitude,
            "longitude" to userLongitude,
            "driverType" to driverType
//            "latitude" to "30.7046", "longitude" to "76.7179", "driverType" to "1"
        )
        Log.e("TAG", "nearByDriverApi: $map")
        authViewModel.makPostApiCall(this, NEAR_BY_DRIVER, "", true, map)
    }

    private fun bookDriverApi() {
        val map = hashMapOf(
            "_id" to driverId,
            "tripStart" to tripStart,
            "tripEnd" to tripEnd,
            "startTime" to startTime,
            "companyName" to companyName,
            "date" to userDate,
            "status" to status,
            "startLatitude" to startLatitude,
            "startLongitude" to startLongitude,
            "endLatitude" to endLatitude,
            "endLongitude" to endLongitude,
            "flightName" to flightDetails,
            "flightNumber" to flightNo,
            "flightArivalTime" to flightArrivalTime,
            "flightGateNumber" to flightGateNo,
            "locationType" to locationType,
            "price" to price,
        )
        Log.e("TAG", "bookDriverApi: $map")
        authViewModel.makPostApiCall(this, BOOK_DRIVER, "", true, map)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {

                            NEAR_BY_DRIVER -> {
                                val result: NearByDriverResponseModel = fromJson(response.obj!!)
                                nearByDriverList.clear()
                                nearByDriverList.addAll(result.body!!)

                                if (nearByDriverList.isNotEmpty()) {
                                    binding.tvNoFound.gone()
                                    binding.rvNearByDrivers.visible()
                                    setUpAdapter()
                                } else {
                                    binding.rvNearByDrivers.gone()
                                    binding.tvNoFound.visible()
                                }

                            }

                            BOOK_DRIVER -> {
                                val result: BookDriverResponseModel = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    showDialog()
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


    private fun showDialog() {
        val dialogBinding = RequestSendBottomSheetBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        // Set dialog properties (position, width, background)
        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }

        // Handle button clicks
        dialogBinding.btnOk.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        // Show the dialog
        dialog.show()
    }

}