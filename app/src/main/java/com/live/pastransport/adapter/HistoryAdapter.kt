package com.live.pastransport.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.pastransport.R
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.databinding.ItemHistoryBinding
import com.live.pastransport.home.activity.StartTripDetailActivity
import com.live.pastransport.responseModel.BookingCommonResponseModel
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.getStaticMapUrl
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class HistoryAdapter(
    private var context: Context, var type: String,
    private val commonList: MutableList<BookingCommonResponseModel>
) :
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {

    var onHistoryClickListener: ((pos: Int) -> Unit)? = null

    inner class MyViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
            val data = commonList[pos]
            with(binding) {
                if (!data.startLatitude.isNullOrEmpty() && !data.startLongitude.isNullOrEmpty()
                    && !data.endLatitude.isNullOrEmpty() && !data.endLongitude.isNullOrEmpty()
                ) {
                    // Fetch and load static map with route
                    CoroutineScope(Dispatchers.Main).launch {
                        val url = getStaticMapUrl(
                            context,
                            data.startLatitude.toDouble(),
                            data.startLongitude.toDouble(),
                            data.endLatitude.toDouble(),
                            data.endLongitude.toDouble()
                        )
                        Log.d("Map URL", url)
                        Glide.with(context).load(url).into(ivMap)
                    }
                }

                if (getPrefrence("userType", "") == "user") {
                    llUserSide.visible()
                    llDriverSide.gone()
                    tvUserDate.text = data.date?.toString()
                    tvUserTime.text = data.startTime?.toString()
                    tvCompanyName.text = "Company Name:- ${data?.companyName.orEmpty()}"

                    val driverType = data?.driverId?.driverType ?: 0
                    tvDriverType.text = "Driver Type:- ${if (driverType == 0) "Unarmed" else "Armed"}"

                    tvCarName.text = "Car Model:- ${data?.driverId?.carModel.orEmpty()}"


                    when{
                        data.status == 0 ->{
                            tvUserStatus.text = context.getString(R.string.pending)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                        }

                        data.status == 1 ->{
                            tvUserStatus.text = context.getString(R.string.accepted)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                        }

                        data.status == 2 ->{
                            tvUserStatus.text = context.getString(R.string.ongoing)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                        }
                        data.status == 4 ->{
                            tvUserStatus.text = context.getString(R.string.cancelled)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        data.paymentDone == 0 -> {
                            tvUserStatus.text = context.getString(R.string.payment_pending)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        data.status == 3 ||  data.status == 5 -> {
                            tvUserStatus.text = context.getString(R.string.completed)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.parrot))
                        }



                        else -> {
                            tvUserStatus.text = context.getString(R.string.cancelled)
                            tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                    }
                    root.setOnClickListener {
                        onHistoryClickListener?.invoke(pos)
                    }

                    Glide.with(context).load(IMAGE_URL + data.driverId?.image)
                        .placeholder(R.drawable.profile_placeholder).into(ivUserProfile)
                    tvUserName.text = data.driverId?.firstName



                    if (data.driverId?.avgRating.toString().isNotEmpty()) {
                        tvRating.text = String.format(Locale.US, "%.1f", data.driverId?.avgRating)
                        ratingDriver.rating = data.driverId?.avgRating?.toFloat()!!
                    }

                    if (data.price.toString().isNotEmpty()) {
                        ("$" + String.format(Locale.US, "%.1f", data.price)).also {
                            tvPrice.text = it
                        }
                    }

                    "Car Model - ${data.driverId?.carModel}".also { tvCarName.text = it }


                } else {
                    Glide.with(context).load(IMAGE_URL + data.userId?.image)
                        .placeholder(R.drawable.profile_placeholder).into(ivProfile)
                    tvName.text = data.userId?.firstName
                    tvDate.text = data.date?.toString()
                    tvTime.text = data.startTime?.toString()


                    when{
                        data.status == 0 ->{
                            tvDriverStatus.text = context.getString(R.string.pending)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                        }

                        data.status == 1 ->{
                            tvDriverStatus.text = context.getString(R.string.accepted)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                        }

                        data.status == 2 ->{
                            tvDriverStatus.text = context.getString(R.string.ongoing)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                        }
                        data.status == 4 ->{
                            tvDriverStatus.text = context.getString(R.string.cancelled)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        data.paymentDone == 0 -> {
                            tvDriverStatus.text = context.getString(R.string.payment_pending)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        data.status == 3 ||  data.status == 5 -> {
                            tvDriverStatus.text = context.getString(R.string.completed)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.parrot))
                        }



                        else -> {
                            tvDriverStatus.text = context.getString(R.string.cancelled)
                            tvDriverStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                    }

                    llDriverSide.visible()
                    llUserSide.gone()
                    root.setOnClickListener {
                        val intent = Intent(context, StartTripDetailActivity::class.java)
                            .putExtra("bookingId", data._id)
                        context.startActivity(intent)
                    }

                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return commonList.size
    }

}