package com.live.pastransport.home.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
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
import com.google.android.gms.maps.model.PolylineOptions
import com.live.pastransport.R
import com.live.pastransport.base.BOOKING_REQUEST_DETAIL
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.databinding.ActivityStartTripDetailBinding
import com.live.pastransport.databinding.DialogCancelTripBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.UserHistoryDetailsModel
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.utils.LocationUpdateUtilityActivity
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class StartTripDetailActivity : LocationUpdateUtilityActivity(), OnMapReadyCallback,
    SocketManager.Observer {
    private val authViewModel by viewModels<AuthViewModel>()
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var directionPolyline: Polyline? = null
    private var hasAnimatedPolyline = false

    private lateinit var binding: ActivityStartTripDetailBinding
    var from = -1
    var bookingId = ""
    var mapCurrent: GoogleMap? = null
    private lateinit var googleMap: GoogleMap
    var startLatLng: LatLng? = null
    var endLatLng: LatLng? = null
    var status = 0

    var map: MapView? = null
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        MapsInitializer.initialize(this)
        mapCurrent = googleMap

        mapCurrent!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        // Additional map configurations can be done here
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
        if (status == 2) {

            updateDriverLatLngToUser()
            startLatLng =
                LatLng(prefs?.getString("lat")!!.toDouble(), prefs?.getString("lng")!!.toDouble())
            getDirection(startLatLng!!, endLatLng!!)
        }
    }
    private fun updateDriverLatLngToUser(){
        val jsonObjects = JSONObject()
        jsonObjects.put("userId", getPrefrence("UserId", "").toString())
        jsonObjects.put("bookingId", bookingId)
        jsonObjects.put("longitude", startLatLng?.longitude.toString())
        jsonObjects.put("latitude", startLatLng?.latitude.toString())
        Log.e( "updateDriverLatLngToUser: ", jsonObjects.toString())
        socketManager.emitEvent(SocketManager.TRACKING_EMIT, jsonObjects)
    }

    override fun onChangedLocation(lat: Double, lng: Double) {

        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())

        if (status == 2) {
            updateDriverLatLngToUser()

            startLatLng =
                LatLng(prefs?.getString("lat")!!.toDouble(), prefs?.getString("lng")!!.toDouble())
            getDirection(startLatLng!!, endLatLng!!)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!socketManager.isConnected()) {
            socketManager.init()
        }
        bookingId = intent.getStringExtra("bookingId").toString()
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(this)
        viewModelSetupAndResponse()
        historyDetailsApi()
        setClickListener()
        setUserTypeView()
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
                                    val body = result.body

                                    status = body?.status ?: 0

//                                        set data
                                    binding.apply {
                                        tvUserName.text =
                                            body?.userId?.firstName + " " + body?.userId?.lastName
                                        ivProfile.loadImageFromServer(
                                            this@StartTripDetailActivity,
                                            body?.userId?.image
                                        )
                                        date.text = body?.date
                                        time.text = body?.startTime
                                        val timeString = body?.endTime ?: "--"

                                        if (timeString != "--") {
                                            try {
                                                rlEnd.visible()

                                                val utcFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                                                utcFormatter.timeZone = TimeZone.getTimeZone("UTC")

                                                val date = utcFormatter.parse(timeString)

                                                val localFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                                localFormatter.timeZone = TimeZone.getDefault()

                                                val localTime = localFormatter.format(date!!).uppercase(Locale.getDefault())


                                               timeEnd.text = localTime
                                                Log.d("TimeConversion", "Local time: $localTime")

                                            } catch (e: ParseException) {
                                                Log.e("TimeConversion", "Invalid time format: ${e.message}")
                                            }
                                        } else {
                                            rlEnd.gone()
                                            timeEnd.text = "--"
                                        }

                                        tvPickUpLocation.text = body?.tripStart
                                        tvDropOffLocation.text = body?.tripEnd
                                        tvTotalDistance.text =
                                            "Total Distance: ${body?.distance}"

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

                                        ivChat.setOnClickListener {
                                            startActivity(
                                                Intent(
                                                    this@StartTripDetailActivity,
                                                    ChatActivity::class.java
                                                )
                                                    .putExtra("userImage", body?.userId?.image)
                                                    .putExtra("receiverId", body?.userId?._id)
                                                    .putExtra(
                                                        "firstName",
                                                        body?.userId?.firstName
                                                    )
                                                    .putExtra(
                                                        "lastName",
                                                        body?.userId?.lastName
                                                    )
                                            )
                                        }


                                        if (status == 0 || status == 1 || status == 2) {
                                            val currentTime = Calendar.getInstance().time
                                            val formatter =
                                                SimpleDateFormat("hh:mm a", Locale.getDefault())
                                            val formattedTime = formatter.format(currentTime)
                                                .uppercase(Locale.getDefault())
                                            val currentTimeString =
                                                formattedTime.format(currentTime)

                                            val estimateTimeString = body?.estimateTime
                                            if (!estimateTimeString.isNullOrEmpty()) {
                                                var totalMinutes = 0
                                                val components = estimateTimeString.split(" ")

                                                for (i in components.indices) {
                                                    if ((components[i] == "hours" || components[i] == "hour") && i > 0) {
                                                        totalMinutes += (components[i - 1].toIntOrNull()
                                                            ?: 0) * 60
                                                    }
                                                    if ((components[i] == "mins" || components[i] == "min") && i > 0) {
                                                        totalMinutes += components[i - 1].toIntOrNull()
                                                            ?: 0
                                                    }
                                                }

                                                val calendar = Calendar.getInstance()
                                                calendar.add(Calendar.MINUTE, totalMinutes)
                                                val estimatedArrivalTimeString =
                                                    formatter.format(calendar.time)

                                                tvEstimatedTime.text =
                                                    "Estimated Time of Arrival: $estimatedArrivalTimeString"
                                            } else {
                                                tvEstimatedTime.text =
                                                    "Current Time: $currentTimeString"
                                            }
                                        }

                                        when (status) {
                                            0 -> {
                                                tvStatus.text = "Pending"
                                                tvStatus.setTextColor(Color.RED)
                                                btnStartTrip.visibility = View.GONE
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
                                            }

                                            1 -> {
                                                tvStatus.text = "Accepted"
                                                btnStartTrip.visibility = View.VISIBLE
                                                btnCancelRide.visibility = View.VISIBLE
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
                                            }

                                            2 -> {
                                                tvStatus.text = "Start Trip"
                                                btnStartTrip.text = "End Trip"
                                                btnStartTrip.visibility = View.VISIBLE
                                                btnCancelRide.visibility = View.GONE
                                                endLatLng = LatLng(
                                                    body?.endLatitude?.toDouble() ?: 0.0,
                                                    body?.endLongitude?.toDouble() ?: 0.0
                                                )
                                            }

                                            3 -> {
                                                stopLocationUpdates()

                                                startLatLng = LatLng(
                                                    body?.startLatitude?.toDouble() ?: 0.0,
                                                    body?.startLongitude?.toDouble() ?: 0.0
                                                )
                                                endLatLng = LatLng(
                                                    body?.endLatitude?.toDouble() ?: 0.0,
                                                    body?.endLongitude?.toDouble() ?: 0.0
                                                )
                                                getDirection(startLatLng!!, endLatLng!!)
                                                stopLocationUpdates()
                                                tvStatus.text = "End Trip"
                                                tvEstimatedTime.visibility = View.GONE
                                                btnStartTrip.visibility = View.GONE
                                                btnCancelRide.visibility = View.GONE
                                            }

                                            4 -> {
                                                stopLocationUpdates()

                                                startLatLng = LatLng(
                                                    body?.startLatitude?.toDouble() ?: 0.0,
                                                    body?.startLongitude?.toDouble() ?: 0.0
                                                )
                                                endLatLng = LatLng(
                                                    body?.endLatitude?.toDouble() ?: 0.0,
                                                    body?.endLongitude?.toDouble() ?: 0.0
                                                )
                                                getDirection(startLatLng!!, endLatLng!!)

                                                tvStatus.text = "Cancelled"
                                                tvStatus.setTextColor(Color.RED)
                                                btnStartTrip.visibility = View.GONE
                                                btnCancelRide.visibility = View.GONE
                                                tvEstimatedTime.gone()
                                            }

                                            5 -> {
                                                stopLocationUpdates()

                                                startLatLng = LatLng(
                                                    body?.startLatitude?.toDouble() ?: 0.0,
                                                    body?.startLongitude?.toDouble() ?: 0.0
                                                )
                                                endLatLng = LatLng(
                                                    body?.endLatitude?.toDouble() ?: 0.0,
                                                    body?.endLongitude?.toDouble() ?: 0.0
                                                )
                                                getDirection(startLatLng!!, endLatLng!!)

                                                tvEstimatedTime.visibility = View.GONE
                                                btnStartTrip.visibility = View.GONE
                                                tvStatus.text = "Completed"
                                                tvStatus.setTextColor(Color.parseColor("#008000")) // Assuming a 'completed' color

                                                btnCancelRide.visibility = View.GONE

                                                if (body?.paymentDone == 0) {
                                                    tvStatus.text = "Payment Pending"
                                                    tvStatus.setTextColor(Color.RED)
                                                    btnStartTrip.visibility = View.VISIBLE
                                                    btnCancelRide.visibility = View.VISIBLE
                                                }
                                            }
                                        }


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




    private fun getDirection(start: LatLng, end: LatLng) {
        val startBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.start_car)
        val endBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location_icon)

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
                    Log.e("Direction", "Direction status not OK: ${direction?.status}")
                }
            }, onDirectionFailure = { t ->
                Log.e("Direction", "Error fetching direction: ${t.message}")
            })
    }

    override fun onResume() {
        super.onResume()
        getLiveLocation(this@StartTripDetailActivity)
        startLocationUpdates()

        socketManager.registerObserver(this)

        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()

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

    private fun historyDetailsApi() {
        val map = hashMapOf("bookingId" to bookingId)
        authViewModel.makPostApiCall(this, BOOKING_REQUEST_DETAIL, "", true, map)
    }

    private fun setUserTypeView() {

        from = intent.getIntExtra("From", -1)

        with(binding) {
            if (from == 1) {
                llCompletedStatus.visible()
                llButtons.gone()
                tvStatus.gone()
            } else {
                llButtons.visible()
                llCompletedStatus.gone()
            }

        }
    }

    private fun setClickListener() {
        with(binding) {
            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnStartTrip.setOnClickListener {
                if (status == 1) {
                    val jsonObjects = JSONObject()
                    jsonObjects.put("status", "2")
                    jsonObjects.put("bookingId", bookingId)
                    jsonObjects.put("driverId", getPrefrence("UserId", "").toString())
                    Log.d("Socket", jsonObjects.toString())
                    socketManager.emitEvent(SocketManager.BOOKING_ACCEPT_REJECT_EMIT, jsonObjects)

                } else if (status == 2) {

                    val jsonObjects = JSONObject()
                    jsonObjects.put("status", "3")
                    jsonObjects.put("bookingId", bookingId)
                    jsonObjects.put("driverId", getPrefrence("UserId", "").toString())
                    Log.d("Socket", jsonObjects.toString())
                    socketManager.emitEvent(SocketManager.BOOKING_ACCEPT_REJECT_EMIT, jsonObjects)
                }
                historyDetailsApi()

            }

            btnCancelRide.setOnClickListener {
                showDialog()
            }
        }
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
            if (dialogBinding.edReason.text.isEmpty()) {
                Utils.errorAlert(this,"Please enter a valid reason!")
            }
            else{
            val jsonObjects = JSONObject()
            jsonObjects.put("status", "4")
            jsonObjects.put("bookingId", bookingId)
            jsonObjects.put("driverId", getPrefrence("UserId", "").toString())
            Log.d("Socket", jsonObjects.toString())
            socketManager.emitEvent(SocketManager.BOOKING_ACCEPT_REJECT_EMIT, jsonObjects)
            dialog.dismiss()
            finish()}
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onResponseArray(event: String, args: JSONArray) {

    }

    override fun onResponse(event: String, args: JSONObject) {
    }

    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }
}