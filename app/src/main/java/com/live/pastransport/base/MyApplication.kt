package com.live.pastransport.base

import android.app.Application
import android.os.StrictMode
import android.support.multidex.MultiDex
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.live.pastransport.prefs.SharePrefs
import com.live.pastransport.sockets.SocketManager
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class MyApplication : Application(), AppLifecycleHandler.AppLifecycleDelegates {

    companion object {
        val socketManager: SocketManager by lazy { SocketManager() }
        var mInstance: MyApplication? = null
        var prefs: SharePrefs? = null

        private var lifecycleHandler: AppLifecycleHandler? = null

        @Synchronized
        fun getInstance(): MyApplication? {
            return mInstance
        }
    }

    fun clearData() {
        prefs?.clearSharedPreference()
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this);

        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        mInstance = this
        MultiDex.install(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        prefs = SharePrefs(applicationContext)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        val lifecycleHandler = AppLifecycleHandler(this)
        registerActivityLifecycleCallbacks(lifecycleHandler)
        registerComponentCallbacks(lifecycleHandler)

        socketManager.init()
    }

    override fun onAppBackgrounded() {
        Log.e("Application", "Background")
//        mSocketManager?.disconnect()
    }

    override fun onAppForegrounded() {
        Log.e("Application", "Foreground")
        if (!socketManager.isConnected()) {
            socketManager.init()
        }
    }

    private fun registerLifeCycleHandler(lifeCycleHandler: AppLifecycleHandler?) {
        registerActivityLifecycleCallbacks(lifeCycleHandler)
        registerComponentCallbacks(lifeCycleHandler)
    }
}