package com.live.pastransport.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.live.pastransport.R
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.databinding.ItemDriverRequestsBinding
import com.live.pastransport.home.activity.DriverRequestDetailActivity
import com.live.pastransport.responseModel.DriverHomeRequestResponse
import com.live.pastransport.utils.loadImageFromServer

class DriverRequestsAdapter(
    private val context: Context,
    private val requestList: ArrayList<DriverHomeRequestResponse.Body>,
    private val onCityResolved: (
        position: Int,
        pickupLatLng: LatLng,
        dropLatLng: LatLng,
        setCityNames: (pickupCity: String, dropCity: String) -> Unit
    ) -> Unit
) : RecyclerView.Adapter<DriverRequestsAdapter.MyViewHolder>() {
    var onRequestClickListener: ((pos: Int,type: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemDriverRequestsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder.binding) {
            val item = requestList[position]
            ivProfile.loadImageFromServer(context, item.userId.image)
            tvName.text = "${item.userId.firstName} ${item.userId.lastName}"
            tvPickUpLocation.text = item.tripStart
            tvDropOffLocation.text = item.tripEnd
            // 1 for accept , 4 for reject
            btnAccept.setOnClickListener {
                onRequestClickListener?.invoke(position,1)
            }
            btnReject.setOnClickListener {
                onRequestClickListener?.invoke(position,4)
            }
//            val pickupLatLng = LatLng(item.startLatitude.toDoubleOrNull() ?: 0.0, item.startLongitude.toDoubleOrNull() ?: 0.0)
//            val dropLatLng = LatLng(item.endLatitude.toDoubleOrNull() ?: 0.0, item.endLongitude.toDoubleOrNull() ?: 0.0)
//
//            Log.d("Adapter", "Position: $position | Pickup: $pickupLatLng | Drop: $dropLatLng")

//            onCityResolved(position, pickupLatLng, dropLatLng) { pickupCity, dropCity ->
//                tvPickUpLocation.text = pickupCity
//                tvDropOffLocation.text = dropCity
//            }


            // Uncomment if needed
             root.setOnClickListener {
                 context.startActivity(Intent(context, DriverRequestDetailActivity::class.java)
                     .putExtra("data",item)
                 )
             }
        }
    }

    override fun getItemCount(): Int = requestList.size

    class MyViewHolder(val binding: ItemDriverRequestsBinding) : RecyclerView.ViewHolder(binding.root)
}
