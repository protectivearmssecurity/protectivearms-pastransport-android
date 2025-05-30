package com.live.pastransport.home.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.AvoidType
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.live.pastransport.R
import com.live.pastransport.base.DRIVER_PRICE
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.databinding.DriverTypeMenuLayoutBinding
import com.live.pastransport.databinding.FragmentUserHomeBinding
import com.live.pastransport.home.activity.AirportDetailsActivity
import com.live.pastransport.home.activity.NearbyDriversActivity
import com.live.pastransport.home.activity.NotificationActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.DriverPriceResponseModel
import com.live.pastransport.utils.LocationPickerUtility
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getAddressFromLatLong
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.savePrefrence
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class UserHomeFragment : LocationPickerUtility(), GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraIdleListener {
    private lateinit var binding: FragmentUserHomeBinding
    private val authViewModel by viewModels<AuthViewModel>()
    private var driverType = "" //driver 0 for unarmed 1 for armed
    private var locationType = "" //location - 0 for Other , 1 for airport
    private var armedPrice: String? = null
    private var unArmedPrice: String? = null
    private var userDate = ""
    private var startTime = ""
    private var flightDetails = ""
    private var flightNo = ""
    private var flightArrivalTime = ""
    private var flightGateNo = ""
    private var startLatitude = ""
    private var startLongitude = ""
    private var endLatitude = ""
    private var endLongitude = ""
    private var userLatitude = ""
    private var userLongitude = ""

    //    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    var selectedDate: Calendar? = null


    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
        /* set login when getting lat lng */
        Log.e("TAG", "updatedLatLng: $lat  $lng")
        binding.tvLocation.text = getAddressFromLatLong(
            requireActivity(), prefs?.getString("lat")?.toDouble(), prefs?.getString("lng")?.toDouble()
        )
//        stopLocationUpdates()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.api_key_map))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapFragment =
            childFragmentManager.findFragmentById(R.id.ivMap) as SupportMapFragment

        mapFragment.onCreate(savedInstanceState)
        callFragmentContainer()

        locationType = arguments?.getString("locationType").toString()
        flightDetails = arguments?.getString("flightDetails").toString()
        flightNo = arguments?.getString("flightNo").toString()
        flightArrivalTime = arguments?.getString("flightArrivalTime").toString()
        flightGateNo = arguments?.getString("flightGateNo").toString()

        Log.e(
            "TAG",
            "onViewCreated: $locationType - $flightDetails - $flightNo - $flightArrivalTime - $flightGateNo",
        )

        getPriceApi()
        viewModelSetupAndResponse()
        setClickListeners()
    }

    private fun callFragmentContainer() {
        mapFragment.getMapAsync(OnMapReadyCallback { mGoogleMap ->
            // Do something with the GoogleMap object
            // For example, you can add markers, set camera position, etc.
            googleMap = mGoogleMap
            if (locationType == "1") {
                getPreferenceData()
            }
            enableMyLocation()
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@OnMapReadyCallback
            }
//            googleMap.isMyLocationEnabled = true
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.apply {
                uiSettings.apply {
                    isMapToolbarEnabled = false
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
//                    isRotateGesturesEnabled = true
//                    isMyLocationButtonEnabled = true
//                    isMapToolbarEnabled = true
                }
                setOnCameraMoveStartedListener(this@UserHomeFragment)
            }
        })
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    userLatitude = location.latitude.toString()
                    userLongitude = location.longitude.toString()

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    // Optional: Add marker
                    val startDrawable = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_loc_start,
                        null
                    )
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(location.latitude, location.longitude))
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    getIcon(60, 60, startDrawable)
                                )
                            ).flat(true)
                    )

//                    googleMap.addMarker(
//                        MarkerOptions().position(currentLatLng).title("You are here")
//                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setClickListeners() {
        with(binding) {
            MyApplication.prefs?.apply {
                ivProfile.loadImageFromServer(requireContext(), getString("IMAGE"))
                tvHeading.text = getString("NAME")
                if (getString("lat")!!.isNotEmpty()){
                tvLocation.text = getAddressFromLatLong(
                    requireActivity(), getString("lat")?.toDouble(), getString("lng")?.toDouble()
                )}
                else{
                    getLiveLocation(requireActivity())
                }
            }

            radio1.buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            radio2.buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)

            notification.setOnClickListener {
                startActivity(Intent(requireContext(), NotificationActivity::class.java))
            }

            ivMyLocation.setOnClickListener {
                enableMyLocation()
            }

            rlAirport.setOnClickListener {
                locationType = "1"
                radio1.isChecked = true
                radio2.isChecked = false
                savePreferenceData()
                startActivity(Intent(requireContext(), AirportDetailsActivity::class.java))
            }

            rlOther.setOnClickListener {
                locationType = "0"
                radio2.isChecked = true
                radio1.isChecked = false
            }

            rlDriverType.setOnClickListener {
                showPopUp(it)
            }


            etDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    ContextThemeWrapper(context, R.style.DatePickerDialogTheme),
                    { view, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                        etDate.setText(formattedDate)

                        // Store selected date for later comparison
                        selectedDate = Calendar.getInstance().apply {
                            set(Calendar.YEAR, selectedYear)
                            set(Calendar.MONTH, selectedMonth)
                            set(Calendar.DAY_OF_MONTH, selectedDay)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }

                        val convertedFormattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                        userDate = convertedFormattedDate
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                datePickerDialog.show()
            }

            etTime.setOnClickListener {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    ContextThemeWrapper(context, R.style.CustomTimePickerDialog),
                    { view, selectedHour, selectedMinute ->

                        val now = Calendar.getInstance()

                        if (selectedDate != null) {
                            val selectedDateTime = Calendar.getInstance().apply {
                                timeInMillis = selectedDate!!.timeInMillis // Start from selected date
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMinute)
                                set(Calendar.SECOND, 0)
                            }

                            if (selectedDateTime.before(now)) {
                                Utils.errorAlert(
                                    requireActivity(),
                                    getString(R.string.please_select_a_future_time)
                                )
                                return@TimePickerDialog
                            }

                            val formattedTime = formatTimeTo12Hour("$selectedHour:$selectedMinute")
                            etTime.setText(formattedTime)
                            startTime = formattedTime
                        } else {
                            Utils.errorAlert(
                                requireActivity(),
                                getString(R.string.please_select_date_first)
                            )
                        }
                    },
                    hour, minute, true
                )
                timePickerDialog.show()
            }


            etPickupLocation.setOnClickListener {
                if (driverType.isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_driver_type)
                    )
                } else {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(requireContext())
                    pickupLocationListener.launch(intent)
                }
            }

            etDropLocation.setOnClickListener {
                if (driverType.isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_driver_type)
                    )
                } else {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(requireContext())
                    dropLocationListener.launch(intent)
                }
            }

            btnContinue.setOnClickListener {
                if (driverType.isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_driver_type)
                    )
                } else if (etPickupLocation.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_start_location)
                    )
                } else if (etDropLocation.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_end_location)
                    )
                } else if (etDate.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_date)
                    )
                } else if (etTime.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_time)
                    )
                } else if (locationType.isEmpty()) {
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.please_select_location_type)
                    )
                } else if (locationType == "1") {
                    if (flightDetails == "null" && flightNo == "null" && flightArrivalTime == "null" && flightGateNo == "null") {
                        Utils.errorAlert(
                            requireActivity(),
                            getString(R.string.please_fill_in_flight_details)
                        )
                    } else {
                        // If flight details are filled in, proceed to the next screen
                        goingNearByScreen()
                    }
                } else {
                    // If locationType is not 1, no flight details are required,
                    goingNearByScreen()
                }

            }
        }
    }

    private fun goingNearByScreen() {
        startActivity(
            Intent(
                requireContext(),
                NearbyDriversActivity::class.java
            ).putExtra("armedType", driverType)
                .putExtra("userLongitude", userLongitude)
                .putExtra("startLatitude", startLatitude)

                .putExtra("locationType", locationType)
                .putExtra("armedPrice", armedPrice)
                .putExtra("userDate", userDate)
                .putExtra("startTime", startTime)
                .putExtra("tripStart", binding.etPickupLocation.text.toString().trim())
                .putExtra("tripEnd", binding.etDropLocation.text.toString().trim())
                .putExtra("userLatitude", userLatitude)
                .putExtra("startLongitude", startLongitude)
                .putExtra("endLatitude", endLatitude)
                .putExtra("endLongitude", endLongitude)
                .putExtra("price", binding.etEstimatedPrice.text.toString().trim())
                .putExtra("flightDetails", flightDetails)
                .putExtra("flightNo", flightNo)
                .putExtra("flightArrivalTime", flightArrivalTime)
                .putExtra("flightGateNo", flightGateNo)
        )
        clearPreferenceData()
    }

    private fun getPriceApi() {
        authViewModel.makeGetApiCall(requireActivity(), DRIVER_PRICE, "", true)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(viewLifecycleOwner) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            DRIVER_PRICE -> {
                                val result: DriverPriceResponseModel = fromJson(response.obj!!)
                                if (result.code == 200) {
//                                    armedPrice = result.body.filter { it.driverType == "0" }
                                    unArmedPrice =
                                        result.body?.firstOrNull { it?.driverType == "0" }?.price

                                    armedPrice =
                                        result.body?.firstOrNull { it?.driverType == "1" }?.price
                                    Log.e(
                                        "TAG",
                                        "DRIVER_PRICE: $armedPrice -- $unArmedPrice",
                                    )
                                }
                            }
                        }
                    }

                    StatusType.ERROR -> Utils.errorAlert(requireActivity(), it.message)

                    else -> Utils.errorAlert(requireActivity(), it.message)
                }
            }
        }
    }

    /* method for pickup addresses and sets */
    private val pickupLocationListener = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            val addressComponents = place.addressComponents
            val formattedAddress = place.address ?: ""
            val addressName = place.name ?: ""
            // Defaults
            var locality = ""
            var areaLevel1 = ""
            var postalCode = ""
            var subLocality = ""
            var subLocality2 = ""


            startLatitude = place.latLng!!.latitude.toString()
            startLongitude = place.latLng!!.longitude.toString()

//            val address = place.address as String
//            val address = place.name as String

// Try to extract desired components
            addressComponents?.asList()?.forEach { component ->
                when {
                    component.types.contains("locality") -> {
                        locality = component.name
                    }

                    component.types.contains("sublocality_level_1") -> {
                        subLocality = component.name
                    }

                    component.types.contains("sublocality_level_2") -> {
                        subLocality2 = component.name
                    }

                    component.types.contains("administrative_area_level_1") -> {
                        areaLevel1 = component.name
                    }

                    component.types.contains("postal_code") -> {
                        postalCode = component.name
                    }
                }
            }

            Log.e("TAG", "addressComponents:  1 $addressComponents")
            // Construct the final address (like iOS format)
            val finalAddress =
                if (postalCode.isNotEmpty() || locality.isNotEmpty()) {
                    if (subLocality.isEmpty()) {
                        "$subLocality2 $locality $postalCode"
                    } else {
                        "$subLocality $locality $postalCode"
                    }
                }
                else {
                    formattedAddress // fallback
                }
            binding.etPickupLocation.setText(finalAddress)

            if (binding.etDropLocation.text.toString().trim().isNotEmpty()) {
                binding.etDropLocation.text.clear()
                binding.etEstimatedPrice.text.clear()
                endLatitude = ""
                endLongitude = ""
            }

        }
    }

    /* method for drop addresses and sets */
    private val dropLocationListener = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            val addressComponents = place.addressComponents
            val formattedAddress = place.address ?: ""
            val addressName = place.name ?: ""
            // Defaults
            var locality = ""
            var areaLevel1 = ""
            var postalCode = ""
            var subLocality = ""
            var subLocality2 = ""

            endLatitude = place.latLng!!.latitude.toString()
            endLongitude = place.latLng!!.longitude.toString()


// Try to extract desired components
            addressComponents?.asList()?.forEach { component ->
                when {
                    component.types.contains("locality") -> {
                        locality = component.name
                    }

                    component.types.contains("sublocality_level_1") -> {
                        subLocality = component.name
                    }

                    component.types.contains("sublocality_level_2") -> {
                        subLocality2 = component.name
                    }

                    component.types.contains("administrative_area_level_1") -> {
                        areaLevel1 = component.name
                    }

                    component.types.contains("postal_code") -> {
                        postalCode = component.name
                    }
                }
            }

            Log.e("TAG", "addressComponents:  1 $addressComponents")
            // Construct the final address (like iOS format)
            val finalAddress =
                if (postalCode.isNotEmpty() || locality.isNotEmpty()) {
                    if (subLocality.isEmpty()) {
                        "$subLocality2 $locality $postalCode"
                    } else {
                        "$subLocality $locality $postalCode"
                    }
                }
                else {
                    formattedAddress // fallback
                }
            binding.etDropLocation.setText(finalAddress)

            if (!startLatitude.isNullOrEmpty() && !startLongitude.isNullOrEmpty()) {
                getDirection(
                    LatLng(startLatitude.toDouble(), startLongitude.toDouble()),
                    LatLng(endLatitude.toDouble(), endLongitude.toDouble())
                )
            }

        }
    }

    private fun getDirection(start: LatLng, end: LatLng) {
        googleMap.clear()
        GoogleDirection.withServerKey(resources.getString(R.string.api_key_map))
            .from(start)
            .to(end)
            .avoid(AvoidType.INDOOR)
            .transportMode(TransportMode.DRIVING)
            .execute(
                onDirectionSuccess = { direction: Direction? ->
                    if (direction != null) {
                        if (direction.isOK) {
                            // Distance in meters (from API)
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                start.latitude, start.longitude,
                                end.latitude, end.longitude, results
                            )

                            val distanceInMeters = results[0]
                            val distanceInMiles = distanceInMeters * 0.000621371

                            calculateFare(
                                distanceInMiles,
                                driverType = driverType,
                                unarmedPrice = unArmedPrice.toString(),
                                armedPrice = armedPrice.toString()
                            ) { finalFare ->
                                val formattedFare = String.format("%.2f", finalFare)
                                binding.etEstimatedPrice.setText(formattedFare)
                            }

                            val directionList: ArrayList<List<LatLng>> = ArrayList()
                            val directionPositionList: ArrayList<LatLng> = ArrayList()
                            for (i in 0 until direction.routeList.size) {
                                for (j in 0 until direction.routeList[i].legList.size) {
                                    directionList.add(direction.routeList[i].legList[j].directionPoint)
                                }
                            }

                            for (k in 0 until directionList.size) {
                                for (l in 0 until directionList[k].size) {
                                    directionPositionList.add(directionList[k][l])
                                }
                            }

                            googleMap.addPolyline(
                                DirectionConverter.createPolyline(
                                    requireContext(), directionPositionList,
                                    5, ContextCompat.getColor(requireContext(), R.color.blue)
                                )
                            )

                            val startDrawable = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_loc_start,
                                null
                            )
                            val endDrawable =
                                ResourcesCompat.getDrawable(resources, R.drawable.ic_loc_end, null)

                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(start.latitude, start.longitude))
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            getIcon(60, 60, startDrawable)
                                        )
                                    ).flat(true)
                            )

                            googleMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(end.latitude, end.longitude)
                                ).icon(
                                    BitmapDescriptorFactory.fromBitmap(getIcon(60, 60, endDrawable))
                                )
                            )

                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(start.latitude, start.longitude), 20F
                                )
                            )

//                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

                            val latlngBuilder = LatLngBounds.Builder()

                            latlngBuilder.include(LatLng(start.latitude, start.longitude))
                            latlngBuilder.include(LatLng(end.latitude, end.longitude))
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), 100)
                            )

                        } else {
                            Utils.errorAlert(
                                requireActivity(),
                                getString(R.string.something_went_wrong_while_getting_directions)
                            )
                        }
                    }
                },
                onDirectionFailure = {
                    // Do something
                    Log.d("TAG", "getDirection: error occurred")
                    Utils.errorAlert(
                        requireActivity(),
                        getString(R.string.something_went_wrong_while_getting_directions)
                    )
                }
            )
    }

    private fun getIcon(height: Int, width: Int, drawable: Drawable?): Bitmap {
        if (drawable == null) {
            throw IllegalArgumentException("Drawable cannot be null")
        }

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

/*    private fun getIcon(height: Int, width: Int, bitmapDrawable: Drawable?): Bitmap {
        val bitMapDrawable = bitmapDrawable as BitmapDrawable
        val b = bitMapDrawable.bitmap
        return Bitmap.createScaledBitmap(b, width, height, false)

    }*/

    private fun calculateFare(
        distanceInMiles: Double,
        driverType: String, // 0 = unarmed, 1 = armed
        unarmedPrice: String,
        armedPrice: String,
        completion: (Double) -> Unit
    ) {
        // Estimate travel duration assuming average speed = 30 mph
        val estimatedDurationInMinutes = (distanceInMiles / 30.0) * 60

        val baseFare = 20.0
        val durationFare = estimatedDurationInMinutes * 1.0
        val minFare = 45.0

        val finalFare = when (driverType) {
            "0" -> { // Unarmed
                val pricePerMile = unarmedPrice.toDoubleOrNull()
                if (pricePerMile != null) {
                    val distanceFare = distanceInMiles * pricePerMile
                    val totalFare = baseFare + distanceFare + durationFare
                    totalFare
                } else {
                    println("Invalid unarmed price format")
                    minFare
                }
            }

            else -> { // Armed
                val pricePerMile = armedPrice.toDoubleOrNull()
                if (pricePerMile != null) {
                    val distanceFare = distanceInMiles * pricePerMile
                    val totalFare = baseFare + distanceFare + durationFare
                    totalFare
                } else {
                    println("Invalid armed price format")
                    minFare
                }
            }
        }

        completion(finalFare)
    }

    private fun showPopUp(view: View) {
        val binding = DriverTypeMenuLayoutBinding.inflate(LayoutInflater.from(requireContext()))

        val popupWindow = PopupWindow(
            binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.isOutsideTouchable = true
        binding.root.setOnClickListener {
            popupWindow.dismiss()
        }
        binding.tvUnarmed.setOnClickListener {
            if (driverType == "1") {
                clearLocationFiled()
            }

            driverType = "0"
            this.binding.tvDiverType.text = getString(R.string.unarmed)

            popupWindow.dismiss()
        }
        binding.tvArmed.setOnClickListener {
            if (driverType == "0") {
                clearLocationFiled()
            }

            driverType = "1"
            this.binding.tvDiverType.text = getString(R.string.armed)

            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(view)
    }

    fun formatTimeTo12Hour(time: String): String {
        val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf24.parse(time)
        return sdf12.format(date).uppercase(Locale.getDefault()) // Capital AM/PM
    }


    private fun clearLocationFiled() {
        Log.e("TAG", "clearLocationFiled: Cleareddd")
        binding.etPickupLocation.text.clear()
        binding.etDropLocation.text.clear()
        binding.etEstimatedPrice.text.clear()
        startLatitude = ""
        startLongitude = ""
        endLatitude = ""
        endLongitude = ""
    }

    private fun getPreferenceData() {
        val type = getPrefrence("driverType", "")
        val startLocation = getPrefrence("startLocation", "")
        val mStartLatitude = getPrefrence("startLatitude", "")
        val mStartLongitude = getPrefrence("startLongitude", "")

        val endLocation = getPrefrence("endLocation", "")
        val mEndLatitude = getPrefrence("endLatitude", "")
        val mEndLongitude = getPrefrence("endLongitude", "")

        val price = getPrefrence("price", "")
        val mUserDate = getPrefrence("userDate", "")
        val userDateFormat = getPrefrence("userDateFormat", "")
        val mStartTime = getPrefrence("startTime", "")

        if (!mStartLatitude.isNullOrEmpty() && !mStartLongitude.isNullOrEmpty()) {
            getDirection(
                LatLng(mStartLatitude.toDouble(), mStartLongitude.toDouble()),
                LatLng(mEndLatitude.toDouble(), mEndLongitude.toDouble())
            )
        }

        if (type == "0") {
            driverType = "0"
            binding.tvDiverType.text = getString(R.string.unarmed)
        } else {
            driverType = "1"
            this.binding.tvDiverType.text = getString(R.string.armed)
        }

        binding.etPickupLocation.setText(startLocation)
        startLatitude = mStartLatitude
        startLongitude = mStartLongitude

        binding.etDropLocation.setText(endLocation)
        endLatitude = mEndLatitude
        endLongitude = mEndLongitude

        binding.etDate.setText(mUserDate)
        userDate = userDateFormat
        binding.etTime.setText(mStartTime)
        startTime = mStartTime

        binding.etEstimatedPrice.setText(price)
        binding.radio1.isChecked = true
        binding.radio2.isChecked = false

    }

    private fun savePreferenceData() {
        savePrefrence("driverType", driverType)
        savePrefrence("startLocation", binding.etPickupLocation.text.toString().trim())
        savePrefrence("startLatitude", startLatitude)
        savePrefrence("startLongitude", startLongitude)
        savePrefrence("endLocation", binding.etDropLocation.text.toString().trim())
        savePrefrence("endLatitude", endLatitude)
        savePrefrence("endLongitude", endLongitude)
        savePrefrence("price", binding.etEstimatedPrice.text.toString().trim())
        savePrefrence("userDate", binding.etDate.text.toString().trim())
        savePrefrence("userDateFormat", userDate)
        savePrefrence("startTime", binding.etTime.text.toString().trim())
    }

    private fun clearPreferenceData() {
        savePrefrence("driverType", "")
        savePrefrence("startLocation", "")
        savePrefrence("startLatitude", "")
        savePrefrence("startLongitude", "")
        savePrefrence("endLocation", "")
        savePrefrence("endLatitude", "")
        savePrefrence("endLongitude", "")
        savePrefrence("userDateFormat", "")
        savePrefrence("price", "")
        savePrefrence("userDate", "")
        savePrefrence("startTime", "")
    }

    override fun onResume() {
        super.onResume()
        mapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment.onLowMemory()
    }

    override fun onCameraMoveStarted(p0: Int) {
        googleMap.setOnCameraIdleListener(this)
    }

    override fun onCameraIdle() {

    }


}