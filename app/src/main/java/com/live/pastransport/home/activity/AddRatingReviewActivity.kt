package com.live.pastransport.home.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.live.pastransport.R
import com.live.pastransport.base.BOOKING_REQUEST_DETAIL
import com.live.pastransport.base.CREATE_PAYMENT_INTENT
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.base.RATE_USER
import com.live.pastransport.base.STRIPE_WEBHOOK_FRONTEND_HIT
import com.live.pastransport.databinding.ActivityAddRatingReviewBinding
import com.live.pastransport.network.StatusType
import com.live.pastransport.responseModel.CommonResponse
import com.live.pastransport.responseModel.PaymentIntentResponse
import com.live.pastransport.responseModel.UserHistoryDetailsModel
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.fromJson
import com.live.pastransport.viewModel.AuthViewModel
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddRatingReviewActivity : AppCompatActivity() {
    var driverId = ""
    var rating = ""
    var image = ""
    var name = ""
    var bookingId = ""
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityAddRatingReviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRatingReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        image=intent.getStringExtra("image").toString()
        rating=intent.getStringExtra("rating").toString()
        name=intent.getStringExtra("name").toString()
        driverId=intent.getStringExtra("driverId").toString()
        bookingId=intent.getStringExtra("bookingId").toString()

        setUI()
        viewModelSetupAndResponse()
        setClickListeners()
    }
    private fun setUI(){
        binding.apply {
            Glide.with(this@AddRatingReviewActivity).load(IMAGE_URL+image).placeholder(R.drawable.profile_placeholder).into(civImage)
            tvName.text=name
            tvRate.text=rating
        }

    }
    private fun viewModelSetupAndResponse() {
        authViewModel.liveDataMap.observe(this) { response ->
            response?.let {
                when (response.status) {
                    StatusType.SUCCESS -> {
                        when (response.key) {

                            RATE_USER -> {
                                val result: CommonResponse = fromJson(response.obj!!)
                                if (result.code == 200) {
                                    startActivity(Intent(this@AddRatingReviewActivity, MainActivity::class.java))
                                    finishAffinity()
                                }

                            }
                        }
                    }

                    StatusType.ERROR -> Utils.errorAlert(this, it.message)

                    else -> Utils.errorAlert(this, it.message)
                }
            }
        }
    }

    private fun callAddReviewApi(){
        val map=HashMap<String,String>()
        map["driverId"]=driverId
        map["bookingId"]=bookingId
        map["rating"]=binding.ratingBar.rating.toString()
        map["comment"]=binding.edReview.text.toString()
        authViewModel.makPostApiCall(this,RATE_USER,"",true,map)
    }

    private fun setClickListeners() {
        with(binding) {
            btnSubmit.setOnClickListener {

                val rating = ratingBar.rating
                val reviewText = edReview.text.toString().trim()

                if (rating == 0f) {
                    Utils.errorAlert(this@AddRatingReviewActivity,"Please give a rating.")
                    return@setOnClickListener
                }

                if (reviewText.isEmpty()) {
                    edReview.error = "Please enter your review"
                    edReview.requestFocus()
                    return@setOnClickListener
                }

                // If both rating and review are valid
                callAddReviewApi()
            }

            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}