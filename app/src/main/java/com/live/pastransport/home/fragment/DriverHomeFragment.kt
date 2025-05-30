package com.live.pastransport.home.fragment

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.live.pastransport.adapter.DriverRequestsAdapter
import com.live.pastransport.base.BOOK_DRIVER_REQUEST
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.databinding.FragmentDriverHomeBinding
import com.live.pastransport.home.activity.NotificationActivity
import com.live.pastransport.home.activity.StartTripDetailActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.DriverHomeRequestResponse
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.utils.LocationPickerUtility
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.getAddressFromLatLong
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class DriverHomeFragment : LocationPickerUtility(), SocketManager.Observer {
    private val authViewModel by viewModels<AuthViewModel>()
    var requestList = ArrayList<DriverHomeRequestResponse.Body>()
    var bookingId = ""
    private lateinit var binding: FragmentDriverHomeBinding
    private lateinit var adapter: DriverRequestsAdapter
    override fun updatedLatLng(lat: Double, lng: Double) {
        prefs?.saveString("lat", lat.toString())
        prefs?.saveString("lng", lng.toString())
        Log.e("TAG", "updatedLatLng: $lat  $lng")
        binding.tvLocation.text = getAddressFromLatLong(
            requireActivity(),
            prefs?.getString("lat")?.toDouble(),
            prefs?.getString("lng")?.toDouble()
        )
//        stopLocationUpdates()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDriverHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!socketManager.isConnected()) {
            socketManager.init()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Call your data refresh logic here
            requestListApi()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        socketManager.newRequestDriverHomeListener()
        socketManager.bookingAcceptRejectListener()
        viewModelSetupAndResponse()
        setClickListener()
    }

    override fun onResume() {
        super.onResume()
        bookingId = ""
        requestListApi()
        socketManager.registerObserver(this)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(viewLifecycleOwner) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            BOOK_DRIVER_REQUEST -> {
                                val result: DriverHomeRequestResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    requestList.clear()
                                    requestList.addAll(result.body)
                                    if (requestList.isEmpty()) {

                                        binding.tvNoDataFound.visible()

                                    } else {
                                        binding.tvNoDataFound.gone()

                                    }
                                    binding.tvCount.text =
                                        "Requests(${requestList.size.toString()})"

                                    setAdapter()
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

    private fun requestListApi() {
        authViewModel.makeGetApiCall(requireActivity(), BOOK_DRIVER_REQUEST, "", true)
    }

    private fun setClickListener() {
        with(binding) {
            MyApplication.prefs?.apply {
                ivProfile.loadImageFromServer(requireContext(), getString("IMAGE"))
                tvHeading.text = getString("NAME")
                if (getString("lat")!!.isNotEmpty()) {
                    tvLocation.text = getAddressFromLatLong(
                        requireActivity(),
                        getString("lat")?.toDouble(),
                        getString("lng")?.toDouble()
                    )
                } else {
                    getLiveLocation(requireActivity())
                }
            }
            notification.setOnClickListener {
                startActivity(Intent(requireContext(), NotificationActivity::class.java))
            }
        }
    }

    private fun setAdapter() {
        requestList.reverse()
        adapter = DriverRequestsAdapter(
            requireActivity(),
            requestList
        ) { position, pickupLatLng, dropLatLng, setCityNames ->
//            lifecycleScope.launch {
//                val pickupCity = withContext(Dispatchers.IO) {
//                    getCityNameFromCoordinates(requireContext(), pickupLatLng.latitude, pickupLatLng.longitude)
//                }
//                val dropCity = withContext(Dispatchers.IO) {
//                    getCityNameFromCoordinates(requireContext(), dropLatLng.latitude, dropLatLng.longitude)
//                }
//
//                Log.d("CityResolve", "Position $position → PickupCity: $pickupCity | DropCity: $dropCity")
//                setCityNames(pickupCity, dropCity)
//            }
        }
        binding.rvRequests.adapter = adapter
        adapter.onRequestClickListener = { pos, type ->
            bookingId = ""
            val jsonObjects = JSONObject()
            jsonObjects.put("status", type)
            jsonObjects.put("bookingId", requestList[pos]._id)
            jsonObjects.put("driverId", requestList[pos].driverId._id)
            Log.d("Socket", jsonObjects.toString())
            socketManager.emitEvent(SocketManager.BOOKING_ACCEPT_REJECT_EMIT, jsonObjects)
            bookingId = requestList[pos]._id

            if (requestList.isEmpty()) {
                binding.apply {
                    tvNoDataFound.visible()
                    binding.tvCount.text = "Requests(${requestList.size.toString()})"
                }
            }
//            if (type == 1) {
//                startActivity(
//                    Intent(requireActivity(), StartTripDetailActivity::class.java)
//                        .putExtra("bookingId", bookingId)
//                )
//
//            }

        }
    }

    override fun onResponseArray(event: String, args: JSONArray) {

    }

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {
            SocketManager.REQUEST_SEND_TO_DRIVER_LISTENER -> {
                requireActivity().runOnUiThread {
                    try {
                        if (args != null) {
                            requestListApi()
                        }
                    } catch (e: Exception) {
                        Log.d(ContentValues.TAG, "onResponse: " + e.message)
                    }
                }
            }

            SocketManager.BOOKING_ACCEPT_REJECT_LISTENER -> {
                requireActivity().runOnUiThread {
                    try {
                        if (args != null) {
                            Log.e("onResponse: ", args.toString())

                            val status = args.optInt("status", 0)
                            val message = args.optString("message", "")

                            if (status == 206) {
                                Utils.errorAlert(
                                    requireActivity(),
                                    "You already have another ongoing ride"
                                )
                            } else {
                                if (message == "Your request has been cancelled..") {
                                    requestListApi()
                                } else {
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            StartTripDetailActivity::class.java
                                        )
                                            .putExtra("bookingId", bookingId)
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