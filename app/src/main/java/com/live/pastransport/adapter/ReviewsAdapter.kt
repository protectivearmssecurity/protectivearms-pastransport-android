package com.live.pastransport.adapter

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.live.pastransport.databinding.ItemNearByDriverBinding
import com.live.pastransport.databinding.ItemReviewsBinding
import com.live.pastransport.databinding.ReportBottomSheetBinding
import com.live.pastransport.databinding.RequestSendBottomSheetBinding
import com.live.pastransport.home.activity.ChatActivity
import com.live.pastransport.home.activity.MainActivity

class ReviewsAdapter : RecyclerView.Adapter<ReviewsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemReviewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder.binding) {
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    class MyViewHolder(var binding: ItemReviewsBinding) : RecyclerView.ViewHolder(binding.root)

}
