package com.live.pastransport.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.pastransport.databinding.ItemChatBinding
import com.live.pastransport.responseModel.MessagesModel
import com.live.pastransport.utils.convertTimeToAgo
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.visible

class ChatAdapter(
    private var context: Context,
    private val list: ArrayList<MessagesModel.Getdata>
) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnables = mutableMapOf<Int, Runnable>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        with(holder.binding) {
            if (model.senderId?._id.toString() == getPrefrence("UserId", "")) {
                viewSend.visible()
                viewReceived.gone()
                ivProfile2.loadImageFromServer(context, model.senderId?.image)
                textView2.text = model.message
                tvSendTime.text = convertTimeToAgo(model.createdAt)
                startTimeUpdater(tvSendTime, model.createdAt, position)
            } else if (model.receiverId?._id.toString() == getPrefrence("UserId", "")) {
                viewSend.gone()
                viewReceived.visible()
                ivProfile1.loadImageFromServer(context, model.senderId?.image)
                textView.text = model.message
                tvReceivedTime.text = convertTimeToAgo(model.createdAt)
                startTimeUpdater(tvReceivedTime, model.createdAt, position)
            } else {
                viewSend.visible()
                viewReceived.gone()
                ivProfile2.loadImageFromServer(context, model.receiverId?.image)
                textView2.text = model.message
                tvSendTime.text = convertTimeToAgo(model.createdAt)
                startTimeUpdater(tvSendTime, model.createdAt, position)
            }
        }
    }

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

    override fun getItemCount(): Int = list.size

    class MyViewHolder(var binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)
}
