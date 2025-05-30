package com.live.pastransport.adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.pastransport.R
import com.live.pastransport.adapter.ChatAdapter.MyViewHolder
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.databinding.ItemNotificationBinding
import com.live.pastransport.home.activity.ChatActivity
import com.live.pastransport.home.activity.NotificationActivity
import com.live.pastransport.responseModel.NotificationListResponse
import com.live.pastransport.utils.convertTimeToAgo
import com.live.pastransport.utils.timeAgoSinceDate
import com.live.pastransport.utils.toLocalTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NotificationAdapter(
  var   context: Context,
   var  list: ArrayList<NotificationListResponse.Body.Notification>
) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder.binding){
            tvName.text=list[position].senderId.firstName?:""
            tvMessage.text=list[position].message?:""
            Glide.with(context).load(IMAGE_URL+list[position].senderId.image?:"").placeholder(R.drawable.profile_placeholder).into(ivProfile)

            tvTime.text = convertTimeToAgo(list[position].createdAt)
            startTimeUpdater(tvTime, list[position].createdAt, position)

//            root.setOnClickListener {
//                root.context.startActivity(Intent(root.context, ChatActivity::class.java))
//            }
        }
    }
    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnables = mutableMapOf<Int, Runnable>()
    private fun startTimeUpdater(view: android.widget.TextView, createdAt: String?, position: Int) {
        // Cancel if already running for this position
        timeUpdateRunnables[position]?.let { handler.removeCallbacks(it) }

        val runnable = object : Runnable {
            override fun run() {
                view.text = convertTimeToAgo(createdAt)
                handler.postDelayed(this, 30 * 1000) // Repeat every 1 min
            }
        }

        timeUpdateRunnables[position] = runnable
        handler.post(runnable)
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        val position = holder.adapterPosition
        timeUpdateRunnables[position]?.let {
            handler.removeCallbacks(it)
            timeUpdateRunnables.remove(position)
        }
    }
    override fun getItemCount(): Int
    {
        return list.size
    }

    class MyViewHolder(var binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
    {
    }
}