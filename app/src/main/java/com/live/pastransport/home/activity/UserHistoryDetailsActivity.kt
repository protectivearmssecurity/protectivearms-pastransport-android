package com.live.pastransport.home.activity

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.gson.Gson
import com.live.pastransport.R
import com.live.pastransport.base.BOOKING_REQUEST_DETAIL
import com.live.pastransport.base.CREATE_PAYMENT_INTENT
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.base.STRIPE_WEBHOOK_FRONTEND_HIT
import com.live.pastransport.databinding.ActivityUserHistoryDetailsBinding
import com.live.pastransport.databinding.PayNowBottomSheetBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.ChatModel
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.responseModel.MessagesModel
import com.live.pastransport.responseModel.PaymentIntentResponse
import com.live.pastransport.responseModel.UserHistoryDetailsModel
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class UserHistoryDetailsActivity : AppCompatActivity(), OnMapReadyCallback, SocketManager.Observer {
    private lateinit var binding: ActivityUserHistoryDetailsBinding
    private var bookingId = ""
    private var totalAmount = ""
    private var driverId = ""
    private var chargedAmount = ""
    private var driverType = ""
    private var statusCheck = -1
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var directionPolyline: Polyline? = null
    private var hasAnimatedPolyline = false
    private val authViewModel by viewModels<AuthViewModel>()
    var mapCurrent: GoogleMap? = null
    var map: MapView? = null
    private lateinit var googleMap: GoogleMap
    var startLatLng: LatLng? = null
    var endLatLng: LatLng? = null
    private lateinit var paymentSheet: PaymentSheet
    var transactionId = ""
    var publishKey = ""
    var clientSecret = ""
    var customerId = ""
    var customerEphemeralKeySecret = ""

    private var historyDetailList: UserHistoryDetailsModel.Body? = null

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        MapsInitializer.initialize(this)
        mapCurrent = googleMap

        mapCurrent!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        // Additional map configurations can be done here
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserHistoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!socketManager.isConnected()) {
            socketManager.init()
        }
        socketManager.locationUpdateListener()

        bookingId = intent.getStringExtra("bookingId").toString()
        driverType = intent.getStringExtra("driverType").toString()
        Log.e("onCreate-------: ",bookingId )
        Log.e("onCreate-------: ",driverType )
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(this)
        historyDetailsApi()
        viewModelSetupAndResponse()
        setClickListener()
        initializeStripePayment()
    }

    private fun initializeStripePayment() {
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    private fun onPayClicked(
        customer_id: String, ephemeralKey: String, paymentIntentClientSecret: String
    ) {
        val configuration = PaymentSheet.Configuration(
            getString(R.string.app_name),
            PaymentSheet.CustomerConfiguration(customer_id, ephemeralKey),

            )
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                Log.d(
                    ContentValues.TAG,
                    "onPaymentSheetResult: Your payment has been done successfully"
                )

                callWebHookApi()

            }

            is PaymentSheetResult.Canceled -> {
                Utils.errorAlert(this@UserHistoryDetailsActivity,"Payment canceled!")

                Log.d("Payment failed", "Payment canceled!")
            }

            is PaymentSheetResult.Failed -> {
                Utils.errorAlert(this@UserHistoryDetailsActivity,"onPaymentSheetResult: Payment failed " + paymentResult.error.localizedMessage?.toString())

                Log.d(
                    ContentValues.TAG,
                    "onPaymentSheetResult: Payment failed " + paymentResult.error.localizedMessage?.toString()!!
                )
            }
        }
    }

    private fun callCreatePayIntentApi() {
        val map = java.util.HashMap<String, String>()
        map["totalAmount"] = totalAmount
        map["chargedAmount"] = chargedAmount
        map["bookingId"] = bookingId
        authViewModel.makPostApiCall(this, CREATE_PAYMENT_INTENT, "", false, map)
    }

    private fun callWebHookApi() {
        val map = java.util.HashMap<String, String>()
        map["transactionId"] = transactionId
        authViewModel.makPostApiCall(this, STRIPE_WEBHOOK_FRONTEND_HIT, "", false, map)
    }

    private fun setClickListener() {
        with(binding) {
            ratings.setOnClickListener {
//                startActivity(Intent(this@UserHistoryDetailsActivity, ReviewActivity::class.java))
            }

            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivMessage.setOnClickListener {
                startActivity(
                    Intent(this@UserHistoryDetailsActivity, ChatActivity::class.java)
                        .putExtra("userImage", historyDetailList?.driverId?.image)
                        .putExtra("receiverId", historyDetailList?.driverId?._id)
                        .putExtra("firstName", historyDetailList?.driverId?.firstName)
                        .putExtra("lastName", historyDetailList?.driverId?.lastName)
                )
            }


        }
    }

    private fun historyDetailsApi() {
        val map = hashMapOf("bookingId" to bookingId)
        authViewModel.makPostApiCall(this, BOOKING_REQUEST_DETAIL, "", true, map)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            BOOKING_REQUEST_DETAIL -> {
                                val result: UserHistoryDetailsModel = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    historyDetailList = result.body

                                    if (historyDetailList != null) {
                                        getHistoryDetailsData(historyDetailList)
                                    }
                                }

                            }

                            CREATE_PAYMENT_INTENT -> {
                                val result: PaymentIntentResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    transactionId = result.body.transactionId
                                    clientSecret = result.body.paymentIntent
                                    publishKey = result.body.publishableKey
                                    customerId = result.body.customer
                                    customerEphemeralKeySecret = result.body.ephemeralKey
                                    PaymentConfiguration.init(this, publishKey)
                                    onPayClicked(
                                        customerId, customerEphemeralKeySecret, clientSecret
                                    )
                                }

                            }

                            STRIPE_WEBHOOK_FRONTEND_HIT -> {
                                val result: CommonResponse = fromJson(response.obj!!)
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

    private fun getHistoryDetailsData(body: UserHistoryDetailsModel.Body?) {
        binding.apply {

            btnComplete.setOnClickListener {
                if (btnComplete.text == getString(R.string.pay_now)) {
                    callCreatePayIntentApi()
                } else {
                    startActivity(
                        Intent(
                            this@UserHistoryDetailsActivity,
                            AddRatingReviewActivity::class.java
                        )
                        .putExtra("driverId",driverId)
                        .putExtra("bookingId",bookingId)
                        .putExtra("image",body?.driverId?.image)
                        .putExtra("rating",body?.driverId?.avgRating.toString())
                        .putExtra("name",body?.driverId?.firstName+" "+body?.driverId?.lastName)
                    )
                }
            }


            if (!body?.startLatitude.isNullOrEmpty() && !body?.startLongitude.isNullOrEmpty()
                && !body?.endLatitude.isNullOrEmpty() && !body?.endLongitude.isNullOrEmpty()
            ) {
                 startLatLng = LatLng(
                    body?.startLatitude?.toDouble() ?: 0.0,
                    body?.startLongitude?.toDouble() ?: 0.0
                )
                 endLatLng = LatLng(
                    body?.endLatitude?.toDouble() ?: 0.0,
                    body?.endLongitude?.toDouble() ?: 0.0
                )
                getDirection(startLatLng!!, endLatLng!!)
            }
            driverId = body?.driverId?._id.toString()
            totalAmount = body?.price.toString()
            chargedAmount = body?.price.toString()
            "${body?.userId?.firstName} ${body?.userId?.lastName}".also { tvName.text = it }
            tvPickUpLocation.text = body?.tripStart.toString()
            tvDropOffLocation.text = body?.tripEnd.toString()
            ratingDriver.rating = body?.driverId!!.avgRating!!
            tvRating.text = body?.driverId!!.avgRating.toString()
            company.text = body?.companyName.toString()
            "$${String.format("%.2f", body?.price)}".also { price.text = it }
            date.text = body?.date.toString()
            tvTripDate.text = body?.date.toString()
            "${body?.startTime}".also { time.text = it }
            "${body?.startTime}".also { tvTripTime.text = it }


            val isoDate = body?.updatedAt
            if (!isoDate.isNullOrEmpty()) {


                tvTripDate.text = formatDateToReadable(isoDate)


            } else {
                tvTripDate.text = "No date available"
            }

            val status = body?.status ?: 0
            if (status == 0 || status == 1 || status == 2) {
                val currentTime = Date()
                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val estimateTimeString = body?.estimateTime
                if (!estimateTimeString.isNullOrEmpty()) {
                    var totalMinutes = 0
                    val components = estimateTimeString.split(" ")

                    for (i in components.indices) {
                        when (components[i]) {
                            "hour", "hours" -> components.getOrNull(i - 1)?.toIntOrNull()?.let {
                                totalMinutes += it * 60
                            }
                            "min", "mins" -> components.getOrNull(i - 1)?.toIntOrNull()?.let {
                                totalMinutes += it
                            }
                        }
                    }

                    val calendar = Calendar.getInstance()
                    calendar.time = currentTime
                    calendar.add(Calendar.MINUTE, totalMinutes)

                    val estimatedArrivalTimeString = formatter.format(calendar.time)
                    tvTripTime.text = "$estimatedArrivalTimeString"
                } else {
                    tvTripTime.text = formatter.format(currentTime)
                }
            } else if (status == 3 || status == 5) {
                tvTitleText.text = "Date & End Time"
                val timeString = body?.endTime ?: "--"
                val utcFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                utcFormatter.timeZone = TimeZone.getTimeZone("UTC")

                val utcDate = try {
                    utcFormatter.parse(timeString)
                } catch (e: Exception) {
                    null
                }

                if (utcDate != null) {
                    val localFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    localFormatter.timeZone = TimeZone.getDefault()
                    val localTime = localFormatter.format(utcDate)
                    tvTripTime.text = localTime
                    Log.d("ArrivalTime", "Local time: $localTime")
                } else {
                    tvTripTime.text = body?.endTime ?: "--"
                    Log.d("ArrivalTime", "Invalid time format")
                }
            }


            // 0 for other , 1 for airport

            if (body?.locationType == 0) {
                llFlightData.gone()
            } else {
                llFlightData.visible()
                tvFlightNameValue.text = body?.flightName
                tvFlightNumberValue.text = body?.flightNumber
                tvFlightArrivalTimeValue.text =
                    body?.flightArivalTime
                tvExitGateValue.text =
                    body?.flightGateNumber.toString()
            }

            Glide.with(this@UserHistoryDetailsActivity).load(IMAGE_URL + body?.driverId?.image)
                .placeholder(R.drawable.profile_placeholder).into(ivProfile)

            "${body?.driverId?.firstName} ${body?.driverId?.lastName}".also {
                tvDriverName.text = it
            }
            "Car Model - ${body?.driverId?.carModel}".also { tvCarModel.text = it }

            if (driverType == "0") {
                tvCertification.text = getString(R.string.certificates_in_unarmed_training)
            } else {
                tvCertification.text = getString(R.string.certificates_in_armed_training)
            }

            Glide.with(this@UserHistoryDetailsActivity)
                .load(IMAGE_URL + body?.driverId?.armedCertificates)
                .placeholder(R.drawable.image_placeholder).into(ivArmedCertificate)
            Glide.with(this@UserHistoryDetailsActivity)
                .load(IMAGE_URL + body?.driverId?.drivingLicense)
                .placeholder(R.drawable.image_placeholder).into(ivLicense)

            when (body?.status) {
                0 -> {
                    btnComplete.gone()
                    tvTripStatus.text = getString(R.string.pending)
                    tvTripStatus.setTextColor(
                        ContextCompat.getColor(this@UserHistoryDetailsActivity, R.color.orange)
                    )
                }

                1 -> {
                    btnComplete.gone()
                    tvTripStatus.text = getString(R.string.accepted)
                    tvTripStatus.setTextColor(
                        ContextCompat.getColor(this@UserHistoryDetailsActivity, R.color.orange)
                    )
                }

                2 -> {
                    btnComplete.gone()
                    tvTripStatus.text = getString(R.string.ongoing)
                    tvTripStatus.setTextColor(
                        ContextCompat.getColor(this@UserHistoryDetailsActivity, R.color.orange)
                    )
                    statusCheck = 0
                }

                3, 5 -> {
                    if (body.paymentDone == 1) {
                        if (body.isReviewed == 1) {
                            btnComplete.gone()
                            tvTripStatus.text = getString(R.string.completed)
                            tvTripStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@UserHistoryDetailsActivity,
                                    R.color.parrot
                                )
                            )
                            statusCheck = 1
                        } else {
                            btnComplete.visible()
                            tvTripStatus.text = getString(R.string.completed)
                            tvTripStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@UserHistoryDetailsActivity,
                                    R.color.parrot
                                )
                            )
                            btnComplete.text = getString(R.string.rate_review)
                            statusCheck = 1
                        }
                    } else {
                        btnComplete.visible()
                        tvTripStatus.text = getString(R.string.payment_pending)
                        tvTripStatus.setTextColor(
                            ContextCompat.getColor(this@UserHistoryDetailsActivity, R.color.red)
                        )
                        btnComplete.text = getString(R.string.pay_now)
                    }
                }

                else -> {
                    llEstimatedTime.gone()
                    btnComplete.gone()
                    tvTripStatus.text = getString(R.string.cancelled)
                    tvTripStatus.setTextColor(
                        ContextCompat.getColor(this@UserHistoryDetailsActivity, R.color.red)
                    )
                }
            }
        }
    }
    fun formatDateToReadable(isoDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(isoDateString)

            val outputFormat = SimpleDateFormat("yy-MM-dd", Locale.getDefault())  // 👈 your format here
            outputFormat.timeZone = TimeZone.getDefault()

            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date"
        }
    }


    private fun showDialog() {
        val bottomSheetDialog = Dialog(this)
        val bottomSheetBinding = PayNowBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheetDialog.window?.setGravity(Gravity.BOTTOM)
        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheetDialog.show()
        bottomSheetBinding.btnPayNow.setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun getDirection(start: LatLng, end: LatLng) {
        val startBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.start_car)
        val endBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location_icon)
        Log.e("getDirection: ", start.toString())
        Log.e("getDirection: ", end.toString())
        // Update or add Start Marker
        if (startMarker == null) {
            startMarker = mapCurrent?.addMarker(
                MarkerOptions().position(start).title("Pickup").icon(startBitmapDescriptor)
            )
        } else {
            startMarker?.position = start
        }

        // Update or add End Marker
        if (endMarker == null) {
            endMarker = mapCurrent?.addMarker(
                MarkerOptions().position(end).title("Drop-off").icon(endBitmapDescriptor)
            )
        } else {
            endMarker?.position = end
        }

        GoogleDirection.withServerKey(getString(R.string.api_key_map))
            .from(start)
            .to(end)
            .avoid(AvoidType.INDOOR)
            .transportMode(TransportMode.DRIVING)
            .execute(onDirectionSuccess = { direction ->
                if (direction != null && direction.isOK) {
                    val directionPositionList = ArrayList<LatLng>()
                    direction.routeList.forEach { route ->
                        route.legList.forEach { leg ->
                            directionPositionList.addAll(leg.directionPoint)
                        }
                    }

                    if (!hasAnimatedPolyline) {
                        // Animate polyline the first time
                        directionPolyline = mapCurrent?.addPolyline(
                            DirectionConverter.createPolyline(
                                this,
                                ArrayList(),
                                8,
                                ContextCompat.getColor(this, R.color.green)
                            )
                        )

                        val handler = Handler(Looper.getMainLooper())
                        var index = 1
                        val step = 3
                        val runnable = object : Runnable {
                            override fun run() {
                                if (index < directionPositionList.size) {
                                    val nextIndex = (index + step).coerceAtMost(directionPositionList.size)
                                    directionPolyline?.points = directionPositionList.subList(0, nextIndex)
                                    index = nextIndex
                                    handler.postDelayed(this, 0)
                                }
                            }
                        }
                        handler.post(runnable)
                        hasAnimatedPolyline = true
                    } else {
                        // Just update polyline instantly
                        directionPolyline?.points = directionPositionList
                    }

                    // Camera update if needed
                    if (directionPositionList.isNotEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        directionPositionList.forEach { boundsBuilder.include(it) }
                        val bounds = boundsBuilder.build()
                        mapCurrent?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }

                } else {
                    Log.e("Direction", "Direction fetch failed or status not OK")
                }
            }, onDirectionFailure = { t ->
                Log.e("Direction", "Error fetching direction: ${t.message}")
            })
    }
    override fun onResume() {
        super.onResume()
        socketManager.registerObserver(this)
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        socketManager.unregisterObserver(this)

        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onResponseArray(event: String, args: JSONArray) {
    }

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {
            SocketManager.LOCATION_UPDATED_LISTENER -> {
                runOnUiThread {
                    try {
                        if (args != null) {
                            Log.e("onResponse: ", args.toString())

                            val updatedUser = args.getJSONObject("updatedUser")
                            val location = updatedUser.getJSONObject("location")
                            val coordinates = location.getJSONArray("coordinates")

                            val longitude = coordinates.getDouble(0)
                            val latitude = coordinates.getDouble(1)
                            startLatLng= LatLng(latitude.toDouble(),longitude.toDouble())


                            getDirection(startLatLng!!,endLatLng!!)


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
