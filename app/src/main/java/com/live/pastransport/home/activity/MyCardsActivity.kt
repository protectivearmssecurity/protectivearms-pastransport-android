package com.live.pastransport.home.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.live.pastransport.R
import com.live.pastransport.adapter.MyCardsAdapter
import com.live.pastransport.databinding.ActivityMyCardsBinding
import com.live.pastransport.databinding.PayNowBottomSheetBinding
import com.live.pastransport.home.fragment.HistoryFragment
import com.live.pastransport.home.fragment.UserHomeFragment
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible

class MyCardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyCardsBinding
    private val adapter = MyCardsAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMyCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListeners()
        setAdapters()
        setUserTypeView()
    }

    private fun setUserTypeView() {
        with(binding){
            if (intent.getStringExtra("from") == "payNow") {
                tvHeader.text = getString(R.string.payment_method)
                btnPayNow.visible()
            } else {
                tvHeader.text = getString(R.string.my_cards)
                btnPayNow.gone()
            }
        }
    }

    private fun setAdapters() {
        binding.rvMyCards.adapter = adapter
    }

    private fun setClickListeners() {
         with(binding){
            ivBack.setOnClickListener {
                 finish()
             }
             btnAddNewCard.setOnClickListener {
                 startActivity(Intent(this@MyCardsActivity,AddNewCardActivity::class.java))
             }
             btnPayNow.setOnClickListener {
                 showDialog()
             }
         }
    }
    private fun showDialog() {
        val dialogBinding = PayNowBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(this)  // Use Dialog instead of AlertDialog
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.tvContent.text = getString(R.string.your_payment_has_been_sent_successfully)
        dialogBinding.btnPayNow.text = getString(R.string.done)

        dialogBinding.btnPayNow.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
        dialog.show()
    }

}