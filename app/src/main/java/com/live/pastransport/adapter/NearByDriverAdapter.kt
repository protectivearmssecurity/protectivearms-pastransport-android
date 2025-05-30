package com.live.pastransport.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.pastransport.R
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.databinding.ItemNearByDriverBinding
import com.live.pastransport.responseModel.NearByDriverResponseModel
import java.util.Locale

class NearByDriverAdapter(
    private val nearByList: MutableList<NearByDriverResponseModel.Body?>
) : RecyclerView.Adapter<NearByDriverAdapter.MyViewHolder>() {

    var onNearClickListener: ((pos: Int,type: Int) -> Unit)? = null

    inner class MyViewHolder(private val binding: ItemNearByDriverBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {
            val data = nearByList[pos]
            with(binding) {

                if (data?.image?.isNotEmpty() == true) {
                    Glide.with(binding.root.context).load(IMAGE_URL + data.image)
                        .placeholder(R.drawable.image_placeholder).into(ivDriver)
                }

                "${data?.firstName} ${data?.lastName}".also { tvHeader.text = it }

                //driver 0 for unarmed 1 for armed
                if (data?.driverType == 0) {
                    tvDriverType.text = binding.root.context.getString(R.string.unarmed)
                } else {
                    tvDriverType.text = binding.root.context.getString(R.string.armed)
                }

                tvLocation.text = data?.address.toString()

                if (data?.avgRating.toString().isNotEmpty()) {
                    tvRating.text = String.format(Locale.US, "%.1f", data?.avgRating)
                }
                itemView.setOnClickListener {
                    onNearClickListener?.invoke(pos,0)
                }
                btnSendRequest.setOnClickListener {
                    onNearClickListener?.invoke(pos,1)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemNearByDriverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return nearByList.size
    }

}
