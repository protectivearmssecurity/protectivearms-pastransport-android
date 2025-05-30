package com.live.pastransport.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.pastransport.databinding.ItemMessagesBinding
import com.live.pastransport.home.activity.ChatActivity
import com.live.pastransport.responseModel.MessageInboxListResponse
import com.live.pastransport.utils.convertTimeToAgo
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.loadImageFromServer

class MessageAdapter(private var context: Context, private val list: ArrayList<MessageInboxListResponse.Getdata>):
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding = ItemMessagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder.binding){

            val model = list[position]

            if (model.senderId?._id.toString() == getPrefrence("UserId", "")) {
                tvName.text = "${model.receiverId?.firstName ?: ""} ${model.receiverId?.lastName ?: ""}"
                circleImageView.loadImageFromServer(context, model.receiverId?.image)
            } else {
                tvName.text = "${model.senderId?.firstName ?: ""} ${model.senderId?.lastName ?: ""}"
                circleImageView.loadImageFromServer(context, model.senderId?.image)
            }

            tvCount.text = model.unreadCount?.toString() ?: ""
            tvMsg.text = model.lastmessage?.message ?: ""

            tvCount.visibility = if ((model.unreadCount ?: 0) == 0) {
                View.GONE
            } else {
                View.VISIBLE
            }
            tvTime.text = convertTimeToAgo(model.updatedAt)

            root.setOnClickListener {
                if (model.senderId?._id.toString() == getPrefrence("UserId", "")) {
                    context.startActivity(Intent(root.context, ChatActivity::class.java).apply {
                        putExtra("userImage", model.receiverId?.image.toString())
                        putExtra("receiverId", model.receiverId?._id.toString())
                        putExtra("firstName", model.receiverId?.firstName)
                        putExtra("lastName", model.receiverId?.lastName)
                    })
                } else{
                    context.startActivity(Intent(root.context, ChatActivity::class.java).apply {
                        putExtra("userImage", model.senderId?.image.toString())
                        putExtra("receiverId", model.senderId?._id.toString())
                        putExtra("firstName", model.senderId?.firstName)
                        putExtra("lastName", model.senderId?.lastName)
                    })
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(var binding: ItemMessagesBinding) : RecyclerView.ViewHolder(binding.root)
}