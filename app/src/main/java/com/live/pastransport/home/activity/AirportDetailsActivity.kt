package com.live.pastransport.home.activity

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity
import com.live.pastransport.R
import com.live.pastransport.databinding.ActivityAirportDetailsBinding
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.savePrefrence
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AirportDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAirportDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirportDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListener()
    }

    private fun setUpListener() {
        with(binding) {
            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            etFlightArrivalTime.setOnClickListener {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                // Create the TimePickerDialog with the custom style
                val timePickerDialog = TimePickerDialog(
                    ContextThemeWrapper(
                        this@AirportDetailsActivity, R.style.CustomTimePickerDialog
                    ), // Apply custom theme
                    { view, selectedHour, selectedMinute ->
                        // Format the selected time as HH:mm
                        val time =
                            formatTimeTo12Hour(selectedHour.toString() + ":" + selectedMinute.toString())
                        etFlightArrivalTime.setText(time) // Set the time to the EditText
//                        startTime = time
                    },
                    hour, minute, true,
                )
                timePickerDialog.show()
            }

            btnContinue.setOnClickListener {
                if (etFlightDetails.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        this@AirportDetailsActivity,
                        getString(R.string.please_enter_flight_name)
                    )
                } else if (etFlightNo.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        this@AirportDetailsActivity,
                        getString(R.string.please_enter_flight_number)
                    )
                } else if (etFlightArrivalTime.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        this@AirportDetailsActivity,
                        getString(R.string.please_select_time)
                    )
                } else if (etExitGateNo.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(
                        this@AirportDetailsActivity,
                        getString(R.string.please_enter_gate_number)
                    )
                } else {
                    goingToMainActivity()
                }
            }
        }
    }

    private fun goingToMainActivity() {
        savePrefrence("userType", "user")
        startActivity(
            Intent(this@AirportDetailsActivity, MainActivity::class.java)
                .putExtra("flightDetails", binding.etFlightDetails.text.toString().trim())
                .putExtra("flightNo", binding.etFlightNo.text.toString().trim())
                .putExtra("flightArrivalTime", binding.etFlightArrivalTime.text.toString().trim())
                .putExtra("flightGateNo", binding.etExitGateNo.text.toString().trim())
                .putExtra("locationType", "1")
        )
        finish()
    }

    private fun formatTimeTo12Hour(time: String): String {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(time)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return outputFormat.format(date)
    }

}