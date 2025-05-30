package com.live.pastransport.home.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.live.pastransport.R
import com.live.pastransport.adapter.HistoryAdapter
import com.live.pastransport.base.BOOKING_LIST
import com.live.pastransport.databinding.FragmentHistoryBinding
import com.live.pastransport.home.activity.UserHistoryDetailsActivity
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.BookingCommonResponseModel
import com.live.pastransport.responseModel.BookingHistoryResponseModel
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import com.live.pastransport.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private var type = "0" // 0 -> current, 1-> past
    private val authViewModel by viewModels<AuthViewModel>()
    private val currentList = mutableListOf<BookingHistoryResponseModel.Body.CurrentBooking?>()
    private val pastList = mutableListOf<BookingHistoryResponseModel.Body.PastBooking?>()
    private val commonList = mutableListOf<BookingCommonResponseModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        bookingListApi()
        viewModelSetupAndResponse()
        setClickListeners()
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Call your data refresh logic here
            bookingListApi()
            binding.swipeRefreshLayout.isRefreshing=false
        }
    }

    private fun setClickListeners() {
        with(binding) {
            tvCurrent.setOnClickListener {
                type = "0"
                tabChangeStatus(type)
                bookingListApi()
                binding.rvHistory.scrollToPosition(0)
            }

            tvPast.setOnClickListener {
                type = "1"
                tabChangeStatus(type)
                bookingListApi()
                binding.rvHistory.scrollToPosition(0)
            }
        }
    }

    private fun setAdapter() {
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        } else {
            adapter = HistoryAdapter(requireContext(), type, commonList)
            binding.rvHistory.adapter = adapter
        }


        adapter.onHistoryClickListener = { pos ->
            val intent = Intent(requireActivity(), UserHistoryDetailsActivity::class.java)
            intent.putExtra("bookingId", commonList[pos]._id.toString())
                .putExtra("driverType", commonList[pos].driverId?.driverType.toString()
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        bookingListApi()
    }

    private fun bookingListApi() {
        authViewModel.makeGetApiCall(requireActivity(), BOOKING_LIST, "", true)
    }

    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(viewLifecycleOwner) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {
                            BOOKING_LIST -> {
                                val result: BookingHistoryResponseModel = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    if (type == "0") {
                                        currentList.clear()
                                        currentList.addAll(result.body?.currentBookings!!)

                                        if (currentList.isNotEmpty()) {
                                            binding.tvNoDataFound.gone()
                                            binding.rvHistory.visible()
                                            commonList.clear()
                                            commonList.addAll(currentList.filterNotNull().map {
                                                BookingCommonResponseModel(
                                                    __v = it.__v,
                                                    _id = it._id,
                                                    companyName = it.companyName,
                                                    createdAt = it.createdAt,
                                                    date = it.date,
                                                    driveTime = it.driveTime,
                                                    driverId = it.driverId?.let { driver ->
                                                        BookingCommonResponseModel.DriverId(
                                                            _id = driver._id,
                                                            avgRating = driver.avgRating,
                                                            carModel = driver.carModel,
                                                            driverType = driver.driverType,
                                                            email = driver.email,
                                                            firstName = driver.firstName,
                                                            image = driver.image,
                                                            phone = driver.phone
                                                        )
                                                    },
                                                    endLatitude = it.endLatitude,
                                                    endLongitude = it.endLongitude,
                                                    endTime = it.endTime,
                                                    flightArivalTime = it.flightArivalTime,
                                                    flightGateNumber = it.flightGateNumber,
                                                    flightName = it.flightName,
                                                    flightNumber = it.flightNumber,
                                                    locationType = it.locationType,
                                                    paymentDone = it.paymentDone,
                                                    price = it.price,
                                                    startLatitude = it.startLatitude,
                                                    startLongitude = it.startLongitude,
                                                    startTime = it.startTime,
                                                    status = it.status,
                                                    tripEnd = it.tripEnd,
                                                    tripStart = it.tripStart,
                                                    updatedAt = it.updatedAt,
                                                    userId = it.userId?.let { userid ->
                                                        BookingCommonResponseModel.UserId(
                                                            _id = userid._id,
                                                            email = userid.email,
                                                            firstName = userid.firstName,
                                                            image = userid.image,
                                                            phone = userid.phone
                                                            // map other fields if available
                                                        )
                                                    },
                                                )
                                            })
                                            setAdapter()
                                            Log.e("TAG", "viewModelSetupAndResponse: 1 $commonList")

                                        } else {
                                            binding.rvHistory.gone()
                                            binding.tvNoDataFound.visible()
                                        }

                                    } else {
                                        pastList.clear()
                                        pastList.addAll(result.body?.pastBookings!!)

                                        if (pastList.isNotEmpty()) {
                                            binding.tvNoDataFound.gone()
                                            binding.rvHistory.visible()
                                            commonList.clear()
                                            commonList.addAll(pastList.filterNotNull().map {
                                                BookingCommonResponseModel(
                                                    __v = it.__v,
                                                    _id = it._id,
                                                    companyName = it.companyName,
                                                    createdAt = it.createdAt,
                                                    date = it.date,
                                                    driveTime = it.driveTime,
                                                    driverId = it.driverId?.let { driver ->
                                                        BookingCommonResponseModel.DriverId(
                                                            _id = driver._id,
                                                            avgRating = driver.avgRating,
                                                            carModel = driver.carModel,
                                                            driverType = driver.driverType,
                                                            email = driver.email,
                                                            firstName = driver.firstName,
                                                            image = driver.image,
                                                            phone = driver.phone
                                                        )
                                                    },
                                                    endLatitude = it.endLatitude,
                                                    endLongitude = it.endLongitude,
                                                    endTime = it.endTime,
                                                    flightArivalTime = it.flightArivalTime,
                                                    flightGateNumber = it.flightGateNumber,
                                                    flightName = it.flightName,
                                                    flightNumber = it.flightNumber,
                                                    locationType = it.locationType,
                                                    paymentDone = it.paymentDone,
                                                    price = it.price,
                                                    startLatitude = it.startLatitude,
                                                    startLongitude = it.startLongitude,
                                                    startTime = it.startTime,
                                                    status = it.status,
                                                    tripEnd = it.tripEnd,
                                                    tripStart = it.tripStart,
                                                    updatedAt = it.updatedAt,
                                                    userId = it.userId?.let { userid ->
                                                        BookingCommonResponseModel.UserId(
                                                            _id = userid._id,
                                                            email = userid.email,
                                                            firstName = userid.firstName,
                                                            image = userid.image,
                                                            phone = userid.phone
                                                            // map other fields if available
                                                        )
                                                    },
                                                )
                                            })
                                            setAdapter()
                                            Log.e("TAG", "viewModelSetupAndResponse: 2 $commonList")
                                        } else {
                                            binding.rvHistory.gone()
                                            binding.tvNoDataFound.visible()
                                        }
                                    }
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

    private fun tabChangeStatus(i: String) {
        when (i) {
            "0" -> {
                binding.tvCurrent.setBackgroundResource(R.drawable.button_bg)
                binding.tvPast.setBackgroundResource(R.drawable.black_stroke_button_bg)
                binding.tvCurrent.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.tvPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            }

            "1" -> {
                binding.tvPast.setBackgroundResource(R.drawable.button_bg)
                binding.tvCurrent.setBackgroundResource(R.drawable.black_stroke_button_bg)
                binding.tvPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvCurrent.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }
    }
}