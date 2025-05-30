package com.live.pastransport.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.live.pastransport.R
import com.live.pastransport.auth.activity.ChooseUserTypeActivity
import com.live.pastransport.base.IMAGE_URL
import com.live.pastransport.base.MyApplication
import com.live.pastransport.utils.Utils.getDaysAgoTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Scanner
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun openPdfInExternalViewer(pdfUrl: String,context: Context) {
    val uri = Uri.parse(pdfUrl)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setDataAndType(uri, "application/pdf")
    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
     context.startActivity(intent)

}
fun getCurrentDate(): String {
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return outputFormat.format(Date())
}

fun formatDate(inputDate: String): String {
    // Define the input and output date formats
    val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Parse the input date and format it to the desired output
    val date = inputFormat.parse(inputDate)
    return outputFormat.format(date)
}
fun isDateBeforeToday(date: String): Boolean {
    val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    return selectedDate.before(today)
}
//fun changeLanguage(context: Context) {
//
//    val lang = prefs!!.getPrefrenceLanguage(LANGUAGE, "en").toString()
//
//    Log.e("lang", "changeLanguage: $lang")
//
//    val locale = Locale(lang)
//    Locale.setDefault(locale)
//    val config = Configuration()
//    config.locale = locale
//    context.resources.updateConfiguration(config, null)
//    // If you are in an Activity, recreate it to apply changes immediately
//    if (context is Activity) {
//        context.recreate()
//    }
//}

//fun changeLanguageSplash(context: Context) {
//    val lang = prefs!!.getPrefrenceLanguage(LANGUAGE, "en").toString()
//
//    Log.e("langSplash", "changeLanguage: $lang")
//
//    // Set the new locale
//    val locale = Locale(lang)
//    Locale.setDefault(locale)
//
//    // Create a new Configuration object
//    val config = Configuration(context.resources.configuration)
//    config.setLocale(locale)
//
//    // Update the resources configuration
//    context.resources.updateConfiguration(config, context.resources.displayMetrics)
//
//
//}


fun enablePaddingTop(context: Activity, enableTop:Boolean? = null) {
    val decorView = context.window.decorView
    WindowCompat.setDecorFitsSystemWindows(context.window, false)
    ViewCompat.setOnApplyWindowInsetsListener(decorView) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val top = if (enableTop == true){
            systemBars.top
        }else {
            view.paddingTop
        }
        view.setPadding(systemBars.left, top, systemBars.right, systemBars.bottom)
        insets
    }
    /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
               val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
               v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
               insets
           }*/
}


fun downloadImage(context: Context, imageUrl: String) {
    try {
        // Extract the file extension from the URL
        val uri = Uri.parse(imageUrl)
        val extension = MimeTypeMap.getFileExtensionFromUrl(imageUrl)
            ?: "jpg" // Default to "jpg" if extension is null

        // Create a file name with the correct extension
        val fileName = "image_${System.currentTimeMillis()}.$extension"

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading image")
        request.setDescription("Downloading image from server")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        // Enqueue the download request
        downloadManager.enqueue(request)

        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}



fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Converts a list of image file paths into a list of MultipartBody.Part objects.
 *
 * @param imagePaths List of image file paths.
 * @return List of MultipartBody.Part objects representing the images.
 */
fun createMultipartBodyParts(imagePaths: ArrayList<String>): ArrayList<MultipartBody.Part> {
    val multipartBodyParts = ArrayList<MultipartBody.Part>()

    imagePaths.forEach { imagePath ->
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            val requestBody =
                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
            val multipartBodyPart =
                MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
            multipartBodyParts.add(multipartBodyPart)
        }
    }

    return multipartBodyParts
}

fun showDatePickerDialog(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Calculate the date 18 years ago
    calendar.set(year - 18, month, day)
    val maxDate = calendar.timeInMillis

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date
            val selectedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
            onDateSelected(selectedDate)
        },
        year - 18, // Set the initial year to 18 years ago
        month,
        day
    )

    // Set the maximum date to 18 years ago from today
    datePickerDialog.datePicker.maxDate = maxDate

    datePickerDialog.show()
}

fun convertTimestampTo24HourFormat(timestamp: Long): String {
    // Create a Date object from the timestamp
    val date = Date(timestamp)

    // Define the desired output format
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Format the Date object to the desired output format
    return outputFormat.format(date)
}

fun convertDateFormat(inputDate: String): String {
    // Define the input and output date formats
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Parse the input date string to a Date object
    val date = inputFormat.parse(inputDate)

    // Format the Date object to the desired output format
    return outputFormat.format(date)
}

fun showPdfPreview(pdfFile: File, imageView: ImageView) {
    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null
    var currentPage: PdfRenderer.Page? = null
    try {
        parcelFileDescriptor =
            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
        currentPage = pdfRenderer.openPage(0)
        val bitmap =
            Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        Glide.with(imageView.context)
            .load(bitmap)
            .into(imageView)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        currentPage?.close()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }
}

fun convertDateFormatEditGame(inputDate: String): String {
    // Define the input and output date formats
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    // Parse the input date string to a Date object
    val date = inputFormat.parse(inputDate)

    // Format the Date object to the desired output format
    return outputFormat.format(date)
}

@SuppressLint("SimpleDateFormat")
fun printDateDot(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("dd.MMM.yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}

@SuppressLint("SimpleDateFormat")
fun printDate(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("dd-MMM-yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}

@SuppressLint("SimpleDateFormat")
fun printDateNotification(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("MMMM dd,yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}

fun printTime(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("hh:mm a") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}


fun View.gone() {
    this.visibility = View.GONE
}

fun String.logMessage(prefix: String) {
    Log.e("$prefix: ", this)
}

fun isEmailValid(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    return emailRegex.matches(email)
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun getTimeAgo(textView: TextView, inputTime: String) {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val output = SimpleDateFormat("dd/MM/yyyy")

    var date: Date? = null
    try {
        date = formatter.parse(inputTime)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
//        val formatted = output.format(date)
//        Log.i("DATE", "" + formatted)

    textView.text = getTimeAgoChat(date!!)
}

fun getDaysAgo(textView: TextView, inputTime: String) {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val output = SimpleDateFormat("dd/MM/yyyy")

    var date: Date? = null
    try {
        date = formatter.parse(inputTime)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
//        val formatted = output.format(date)
//        Log.i("DATE", "" + formatted)

    textView.text = getDaysAgoTime(date!!)
}

fun getAddressFromLatLong(context: Context, lat: Double?, longi: Double?): String? {
    if (lat == null || longi == null) {
        return "Invalid latitude or longitude"
    }

    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        // Get the address from latitude and longitude
        val addresses = geocoder.getFromLocation(lat, longi, 1)

        if (addresses?.isNotEmpty() == true) {
            addresses[0].getAddressLine(0) // Return the first address line
        } else {
            "No address found"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Geocoder error: ${e.message}"
    }
}

fun convertTimeToAgo(str_date: String?): String? {
    var convertTime: String? = null
    val suffix = "ago"

    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val passTime = dateFormat.parse(str_date)
        val nowTime = Date()

        val dateDiff: Long = nowTime.time - (passTime?.time ?: 0)

        // Guard against negative differences
        val safeDiff = if (dateDiff < 0) 0 else dateDiff

        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(safeDiff)
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(safeDiff)
        val hours: Long = TimeUnit.MILLISECONDS.toHours(safeDiff)
        val days: Long = TimeUnit.MILLISECONDS.toDays(safeDiff)

        convertTime = when {
            seconds == 0L -> "Just now"
            seconds < 60 -> "$seconds second $suffix"
            minutes < 60 -> "$minutes minute $suffix"
            hours < 24 -> "$hours hour $suffix"
            days < 7 -> "$days days $suffix"
            days > 360 -> "${days / 360} year $suffix"
            days > 30 -> "${days / 30} month $suffix"
            else -> "${days / 7} week $suffix"
        }

    } catch (ex: ParseException) {
        Log.d("TAG", ex.localizedMessage)
    }
    return convertTime
}


public const val SECOND_MILLIS = 1000
public const val MINUTE_MILLIS = 60 * SECOND_MILLIS
public const val HOUR_MILLIS = 60 * MINUTE_MILLIS
public const val DAY_MILLIS = 24 * HOUR_MILLIS
private fun getTimeAgo(inputTime: Date): String? {
    var time = inputTime.time
    if (time < 1000000000000L) {
        time *= 1000
    }
    val now = Date().time
    if (time > now || time <= 0) {
        return "just now"
    }

    val diff = now - time
    return when {
        diff < MINUTE_MILLIS -> {
            "just now"
        }
        diff < 2 * MINUTE_MILLIS -> {
            "1 min"
        }
        diff < 50 * MINUTE_MILLIS -> {
            val dif = diff / MINUTE_MILLIS
            "$dif min"
        }
        diff < 90 * MINUTE_MILLIS -> {
            "1 hour"
        }
        diff < 24 * HOUR_MILLIS -> {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(inputTime)
        }
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(inputTime)
        }
    }
}

private fun getTimeAgoChat(inputTime: Date): String? {
    var time = inputTime.time
    if (time < 1000000000000L) {
        time *= 1000
    }
    val now = Date().time
//    if (time > now || time <= 0) {
//        return "just now"
//    }

    val diff = now - time
    return when {
//        diff < MINUTE_MILLIS -> {
//            "just now"
//        }
//        diff < 2 * MINUTE_MILLIS -> {
//            "1 min"
//        }
//        diff < 50 * MINUTE_MILLIS -> {
//            val dif = diff / MINUTE_MILLIS
//            "$dif min"
//        }
//        diff < 90 * MINUTE_MILLIS -> {
//            "1 hour"
//        }
        diff < 24 * HOUR_MILLIS -> {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.format(inputTime)
        }
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(inputTime)
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun printDateNew(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("yyyy-MM-dd") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}


fun printDate2(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("MMM dd, yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}

fun printDate3(dateAndTime: String?): String {
    //2022-05-16T19:34:11.000Z
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    val date: Date =
        dateFormat.parse(dateAndTime) //You will get date object relative to server/client timezone wherever it is parsed
    val formatter: DateFormat =
        SimpleDateFormat("MMM dd, yyyy") //If you need time just put specific format for time like 'HH:mm:ss'
    val dateStr: String = formatter.format(date)
    return dateStr
}

fun Fragment.sessionExpire() {
//    

    MyApplication.prefs?.clearSharedPreference()
    clearPrefrences()
    this.goToSelectUserWithClearFlag()

}
fun inviteFriends(context: Context) {
    // Here you can add your invitation logic, e.g., sharing an invitation link or message
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Check out this app FliFive!")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Invite friends via"))
}
fun Fragment.goToSelectUserWithClearFlag() {
//    

    MyApplication.prefs?.clearSharedPreference()
    clearPrefrences()
    val intent = Intent(requireActivity(), ChooseUserTypeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    requireActivity().overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    return startActivity(intent)
}
suspend fun getCityNameFromCoordinates(context: Context, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val address = addresses?.firstOrNull()

            Log.d("GeoDebug", """
                Latitude: $lat
                Longitude: $lng
                Full Address: $address
                Locality: ${address?.locality}
                SubAdminArea: ${address?.subAdminArea}
                AdminArea: ${address?.adminArea}
                FeatureName: ${address?.featureName}
                SubLocality: ${address?.subLocality}
            """.trimIndent())

            // Reject known non-city/locality names like "DMC"
            val invalidLocalities = listOf("DMC", "Unknown", "Hospital", "Building")

            val locality = address?.locality
            val city = when {
                locality != null && locality !in invalidLocalities -> locality
                !address?.subAdminArea.isNullOrBlank() -> address?.subAdminArea
                !address?.adminArea.isNullOrBlank() -> address?.adminArea
                else -> "Unknown"
            }

            city ?: "Unknown"
        } catch (e: Exception) {
            Log.e("GeoError", "Failed to get city: ${e.localizedMessage}")
            "Unknown"
        }
    }
}

fun ImageView.loadImageFromServer(
    context: Context,
    imagePath: String?,
    placeholder: Int = R.drawable.profile_placeholder
) {
    val fullUrl = IMAGE_URL + (imagePath ?: "")
    Glide.with(context)
        .load(fullUrl)
        .placeholder(placeholder)
        .into(this)
}

fun ImageView.loadImageFromServerLicense(
    context: Context,
    imagePath: String?,
    placeholder: Int = R.drawable.image_placeholder
) {
    val fullUrl = IMAGE_URL + (imagePath ?: "")
    Glide.with(context)
        .load(fullUrl)
        .placeholder(placeholder)
        .into(this)
}


fun Activity.applyFadeTransition() {
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Fragment.applyFadeTransition() {
    activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.sessionExpire() {
//    

    MyApplication.prefs?.clearSharedPreference()
    clearPrefrences()
    this.goToSelectUserWithClearFlag()
}

// Function to get the current month as a String
fun getCurrentMonth(): String {
    val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
    return dateFormat.format(Date())
}
// Function to open URLs in the browser

fun Activity.goToSelectUserWithClearFlag() {
//    

    MyApplication.prefs?.clearSharedPreference()
    clearPrefrences()
    val intent = Intent(this, ChooseUserTypeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    return startActivity(intent)
}

fun Context.goToSelectUserWithClearFlag() {
//    

//    MyApplication.prefs?.clearSharedPreference()
    clearPrefrences()
    val intent = Intent(this, ChooseUserTypeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    return startActivity(intent)
}


fun saveDeviceTokenPrefrence(key: String, value: Any) {
    val preference = MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
        "device_token",
        0
    )
    val editor = preference.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Boolean -> editor.putBoolean(key, value)
        is Int -> editor.putInt(key, value)
    }
    editor.apply()
}

inline fun <reified T> fromJson(jsonElement: JsonElement): T {
    return Gson().fromJson(jsonElement, T::class.java)
}

inline fun <reified T> getDeviceTokenPrefrence(key: String, deafultValue: T): T {
    val preference = MyApplication.getInstance()!!.applicationContext.getSharedPreferences(
        "device_token",
        0
    )
    return when (T::class) {
        String::class -> preference.getString(key, deafultValue as String) as T
        Boolean::class -> preference.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> preference.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}


fun String.getTextRequestBody(): RequestBody {
    return RequestBody.create("text/plain".toMediaTypeOrNull(), this);
}

fun timeStampToTime(textView: TextView, inputTime: String) {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val output = SimpleDateFormat("dd/MM/yyyy")

    var date: Date? = null
    try {
        date = formatter.parse(inputTime)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    textView.text = getTimeAgo(date!!)
}
fun Date.toLocalTime(): Date {
    val localOffset = TimeZone.getDefault().rawOffset
    return Date(this.time + localOffset)
}
fun Date.timeAgoSinceDate(): String {
    val now = Date()
    val diff = now.time - this.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateFormat.format(this)
        }
    }
}

fun prepareFilePart(partName: String?, file: File): MultipartBody.Part {
    var mediaType: MediaType? = null
    mediaType = if (file.endsWith("png")) {
        "image/png".toMediaTypeOrNull()
    } else {
        "image/jpeg".toMediaTypeOrNull()
    }

    val requestBody = RequestBody.create(mediaType, file)
    return MultipartBody.Part.createFormData(partName.toString(), file.name, requestBody)
}

/* Generates a static map URL with the actual route polyline */
 suspend fun getStaticMapUrl(context: Context, pickupLat: Double, pickupLng: Double, dropoffLat: Double, dropoffLng: Double): String {
    val apiKey = context.getString(R.string.api_key_map) // Your API key
    val routePolyline = getRoutePolyline(pickupLat, pickupLng, dropoffLat, dropoffLng, apiKey)

    return "https://maps.googleapis.com/maps/api/staticmap?" +
            "size=600x300&" + // Set desired size
            "markers=color:green%7Clabel:S%7C$pickupLat,$pickupLng&" +
            "markers=color:red%7Clabel:E%7C$dropoffLat,$dropoffLng&" +
            "path=color:0x1E90FF|weight:5|enc:$routePolyline&" + // Use the actual polyline route
            "key=$apiKey"
}

/* Fetches the route polyline from Google Directions API */
private suspend fun getRoutePolyline(pickupLat: Double, pickupLng: Double, dropOffLat: Double, dropOffLng: Double, apiKey: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$pickupLat,$pickupLng&" +
                    "destination=$dropOffLat,$dropOffLng&" +
                    "key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val scanner = Scanner(connection.inputStream)
            val response = StringBuilder()
            while (scanner.hasNext()) {
                response.append(scanner.nextLine())
            }
            scanner.close()

            // Parse JSON response
            val jsonResponse = JSONObject(response.toString())
            val routes = jsonResponse.getJSONArray("routes")

            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                return@withContext overviewPolyline.getString("points") // Return encoded polyline
            }
        } catch (e: Exception) {
            Log.e("MapError", "Error fetching route polyline", e)
        }
        return@withContext null
    }
}
