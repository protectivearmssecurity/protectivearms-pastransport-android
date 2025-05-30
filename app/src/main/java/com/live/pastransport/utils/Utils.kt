package com.live.pastransport.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.live.pastransport.R
import com.tapadoo.alerter.Alerter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
fun getVideoTimeInSeconds(context: Context, videoUrl: Uri): Int {
    val mp: MediaPlayer = MediaPlayer.create(context, Uri.parse(videoUrl.toString()))
    val duration = mp.duration
    mp.release()
    var str = String.format(TimeUnit.MILLISECONDS.toSeconds(duration.toLong()).toString()).toInt()

    return str
}

object Utils {

    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS
    private const val MONTH_MILLIS = 4 * WEEK_MILLIS.toLong()
    private const val YEAR_MILLIS = 12 * MONTH_MILLIS

    @JvmStatic
    fun successAlert(mContext: Activity?, message: String?) {
        Alerter.create(mContext!!)
            .setTitle(mContext!!.getString(R.string.app_name))
            .setTitleAppearance(R.style.AlertTextAppearanceTitle)
            .setText(message!!)
            .setTextAppearance(R.style.AlertTextAppearanceText)
            .setBackgroundColorRes(R.color.success)
            .setDuration(2000)
            .enableSwipeToDismiss()
            .show()
    }

    @JvmStatic
    fun errorAlert(mContext: Activity?, message: String?) {
        Alerter.create(mContext!!)
            .setTitleAppearance(R.style.AlertTextAppearanceTitle)
            .setText(message!!)
            .setTextAppearance(com.tapadoo.alerter.R.style.AlertTextAppearance)
            .setBackgroundColorRes(R.color.decline_color)
            .setDuration(2000)
            .enableSwipeToDismiss()
            .show()
    }
    @JvmStatic
    fun showNoInternetDialog(activity: Context, retry: () -> Unit) {
        val errorDialog = Dialog(activity)
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        errorDialog.setContentView(R.layout.no_internet_dialog)
        errorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        errorDialog.setCancelable(false)
        errorDialog.setCanceledOnTouchOutside(false)
        errorDialog.window!!.setGravity(Gravity.CENTER)
//        errorDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnTryAgain = errorDialog.findViewById<Button>(R.id.btnTryAgain)

        btnTryAgain.setOnClickListener {
            if (internetAvailability(activity)) {
                errorDialog.dismiss()
                retry()
            }
        }
        errorDialog.show()
    }

    fun internetAvailability(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }


    fun getDaysAgoTime(timeMillis: Date): String? {
        var time = timeMillis.time
        if (time < 1000000000000L) {
            time *= 1000
        }
        val now = System.currentTimeMillis()

        val diff = now - time
        when {
            diff < 2 * SECOND_MILLIS -> {
                return "Just now"
            }
            diff < 60 * SECOND_MILLIS -> {
                return (diff / SECOND_MILLIS).toString() + " sec ago"
            }
            diff < 2 * MINUTE_MILLIS -> {
                return "1 min ago"
            }
            diff < 50 * MINUTE_MILLIS -> {
                return (diff / MINUTE_MILLIS).toString() + " min ago"
            }
            diff < 90 * MINUTE_MILLIS -> {
                return "1 hour ago"
            }
            diff < 24 * HOUR_MILLIS -> {
                return (diff / HOUR_MILLIS).toString() + " hour ago"
            }
            diff < 48 * HOUR_MILLIS -> {
                return "1 day ago"
            }
            diff < 7 * DAY_MILLIS -> {
                return (diff / DAY_MILLIS).toString() + " days ago";
            }
            diff < 2 * WEEK_MILLIS.toLong() -> {
                return "1 week ago"
            }
            diff < 4 * WEEK_MILLIS.toLong() -> {
                return (diff / WEEK_MILLIS.toLong()).toString() + " week ago"
            }
            diff < 2 * MONTH_MILLIS -> {
                return "1 month ago"
            }
            diff < 12 * MONTH_MILLIS -> {
                return (diff / MONTH_MILLIS).toString() + " month ago"
            }
            diff < 2 * YEAR_MILLIS -> {
                return "1 year ago"
            }
            else -> {
                return (diff / YEAR_MILLIS).toString() + " years ago"
            }
        }
    }


    //convert a data class to a map
    fun <T> T.serializeToMap(): Map<String, String> {
        return convert()
    }


    // convert a map to a data class
    inline fun <reified T> Map<String, Any>.toDataClass(): T {
        return convert()
    }

    //convert an object of type I to type O
    inline fun <I, reified O> I.convert(): O {
        val json = Gson().toJson(this)
        return Gson().fromJson(json, object : TypeToken<O>() {}.type)
    }


    fun showToast(mContext: Context?, message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    fun log(activity: Activity, str: String) {
        Log.e(activity.localClassName, str)
    }

    fun formatDateToTime(inputDateString: String): String {
        // Create a SimpleDateFormat to parse the input string (ISO 8601 format)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")  // Since input is in UTC

        // Parse the date string into a Date object
        val date = inputFormat.parse(inputDateString)

        // Create a SimpleDateFormat to format the Date object into the desired time format
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM

        // Format the Date object into the desired time format and return the result
        return outputFormat.format(date)
    }
}