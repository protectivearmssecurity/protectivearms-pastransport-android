package com.live.pastransport.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.live.pastransport.R
import com.live.pastransport.databinding.ItemCardsBinding
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible

class MyCardsAdapter : RecyclerView.Adapter<MyCardsAdapter.MyViewHolder>() {

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCardsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder.binding) {
            root.setOnClickListener {
                selectItem(position)
            }
            if (selectedPosition == position) {
                select.visible()
                unSelect.gone()
                rlCard.setBackgroundResource(R.drawable.select_card_bg)
            } else {
                select.gone()
                unSelect.visible()
                rlCard.setBackgroundResource(R.drawable.unselect_card_bg)
            }
        }
    }

    private fun selectItem(position: Int) {
        if (selectedPosition == position) return

        val previousSelectedPosition = selectedPosition
        selectedPosition = position

        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount(): Int {
        return 4
    }

    class MyViewHolder(var binding: ItemCardsBinding) : RecyclerView.ViewHolder(binding.root)
}
