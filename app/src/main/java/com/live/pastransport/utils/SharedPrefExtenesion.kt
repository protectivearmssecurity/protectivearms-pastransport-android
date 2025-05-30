package com.live.pastransport.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.NON_CLEARABLE_PREFS
import com.live.pastransport.base.SHARED_NAME
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


fun savePrefrence(key: String, value: Any) {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        SHARED_NAME,
        Context.MODE_PRIVATE
    )
    val editor = preference?.edit()

    when (value) {
        is String -> editor?.putString(key, value)
        is Boolean -> editor?.putBoolean(key, value)
        is Int -> editor?.putInt(key, value)
    }
    editor?.apply()
}
// Function to open a URL in the default browser
 fun openUrl(context: Context,url: String) {
    // Create an Intent to view the URL
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(browserIntent)

}

// Function to validate the password
 fun isValidPassword(password: String): Boolean {
    val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
    val passwordMatcher = Regex(passwordPattern)
    return passwordMatcher.matches(password)
}
inline fun <reified T> getPrefrence(key: String, deafultValue: T): T {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        SHARED_NAME,
        Context.MODE_PRIVATE
    )
    return when (T::class) {
        String::class -> preference?.getString(key, deafultValue as String) as T
        Boolean::class -> preference?.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> preference?.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}



 inline fun <reified T> savePrefObject(key: String, obj: T) {
    savePrefrence(key, Gson().toJson(obj))
 }

 inline fun <reified T> getprefObject(key: String): T {
    return Gson().fromJson(getPrefrence(key, ""), T::class.java)
 }


//fun defaultPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
fun clearPrefrences() {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        SHARED_NAME,
        Context.MODE_PRIVATE
    )
    val editor = preference?.edit()
    editor?.clear()
    editor?.apply()
}
 fun getContentAfterCharacter(inputString: String, delimiter: String): String {
    return inputString.substringAfter(delimiter, "")
}
fun isPrefObjectCleared(key: String): Boolean {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        SHARED_NAME,
        0
    )
    return !preference!!.contains(key)
    // Returns true if the key does not exist, indicating that it's cleared
}

fun clearPrefObject(key: String) {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        SHARED_NAME,
        0
    )

    val editor = preference?.edit()
    editor?.remove(key) // Remove the preference associated with the specified key
    editor?.apply()
}

// New functions for non-clearable preferences
fun saveNonClearablePreference(key: String, value: Any) {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        NON_CLEARABLE_PREFS,
        Context.MODE_PRIVATE
    )
    val editor = preference?.edit()

    when (value) {
        is String -> editor?.putString(key, value)
        is Boolean -> editor?.putBoolean(key, value)
        is Int -> editor?.putInt(key, value)
    }
    editor?.apply()
}

inline fun <reified T> getNonClearablePreference(key: String, defaultValue: T): T {
    val preference = MyApplication.getInstance()?.applicationContext?.getSharedPreferences(
        NON_CLEARABLE_PREFS,
        Context.MODE_PRIVATE
    )
    return when (T::class) {
        String::class -> preference?.getString(key, defaultValue as String) as T
        Boolean::class -> preference?.getBoolean(key, defaultValue as Boolean) as T
        Int::class -> preference?.getInt(key, defaultValue as Int) as T
        else -> {
            " " as T
        }
    }
}
