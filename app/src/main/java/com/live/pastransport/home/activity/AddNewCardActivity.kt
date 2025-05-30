package com.live.pastransport.home.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.live.pastransport.R
import com.live.pastransport.databinding.ActivityAddNewCardBinding
import java.util.Calendar

class AddNewCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewCardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityAddNewCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListeners()
    }

    private fun setClickListeners() {
         with(binding){
             ivBack.setOnClickListener {
                 finish()
             }
             btnSave.setOnClickListener {
                 finish()
             }
             etExpDate.setOnClickListener {
                 val calendar = Calendar.getInstance()
                 val year = calendar.get(Calendar.YEAR)
                 val month = calendar.get(Calendar.MONTH)
                 val day = calendar.get(Calendar.DAY_OF_MONTH)

                 val datePickerDialog = DatePickerDialog(
                     ContextThemeWrapper(this@AddNewCardActivity, R.style.DatePickerDialogTheme),
                     { view, selectedYear, selectedMonth, selectedDay ->
                         val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                         etExpDate.setText(formattedDate)
                     },
                     year, month, day
                 )

                 datePickerDialog.show()
             }

         }
    }
}