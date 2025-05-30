package com.live.pastransport.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.live.pastransport.R
import com.live.pastransport.base.IS_NOTIFICATION
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.home.activity.ChatActivity
import com.live.pastransport.home.activity.MainActivity
import com.live.pastransport.home.activity.UserHistoryDetailsActivity
import org.json.JSONObject
import java.util.Date

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MESSAGING_SERVICE"
    private lateinit var resultIntent: PendingIntent
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var mNotificationManager: NotificationManager
    private lateinit var CHANNEL_ID: String

    //    lateinit var intent: Intent
    private var pushType = 0
    private var bookingId = ""
    private var senderId = ""
    private var driverType = ""
    private var msg = ""
    private var title = ""
    private var firstName = ""
    private var lastName = ""
    private var senderImage = ""
    var r: Ringtone? = null

    companion object {
        var player: MediaPlayer? = null
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e("onMessageReceived:00000", remoteMessage.data.toString())

        msg = remoteMessage.data["title"].orEmpty()
        pushType = remoteMessage.data["type"].orEmpty().toIntOrNull() ?: 0
        when (pushType) {
            1 -> {

                val rawSenderDetail = remoteMessage.data["senderDetail"] ?: ""

                if (rawSenderDetail.isNotBlank()) {
                    try {
                        // Step 1: Replace ObjectId(...) with just the string
                        var cleaned = rawSenderDetail.replace(
                            "new ObjectId\\(\"(.*?)\"\\)".toRegex(),
                            "\"$1\""
                        )

                        // Step 2: Replace single quotes with double quotes
                        cleaned = cleaned.replace("'", "\"")

                        // Step 3: Add quotes around keys (if needed)
                        // You might skip this if keys are already quoted after backend fixes.

                        // Now parse as JSONObject
                        val senderDetail = JSONObject(cleaned)

                        senderId = senderDetail.optString("_id")
                        firstName = senderDetail.optString("firstName")
                        lastName = senderDetail.optString("lastName")
                        senderImage = senderDetail.optString("image")
                        title = "$firstName $lastName sent you a message"

                        if (prefs?.getString("STATUS_CHAT") != "true$senderId") {
                            if (prefs?.getInt(IS_NOTIFICATION)==1){
                                createNotification(title, msg, pushType.toString())}                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Push------", "Failed to parse senderDetail: ${e.message}")
                    }
                }

            }

            2 -> {
                bookingId = remoteMessage.data["bookingId"].orEmpty()
                // Step 2: Parse the updatedBookingDetail JSON
                val raw = remoteMessage.data["updatedBookingDetail"].orEmpty()

                // Step 1: Extract the driverId block using regex (including nested content)
                val driverIdBlock =
                    Regex("driverId:\\s*\\{(.*?)\\},\\s*[a-zA-Z]", RegexOption.DOT_MATCHES_ALL)
                        .find(raw)
                        ?.groupValues?.get(1)
                        ?: ""

               // Step 2: From the extracted driverId block, find the driverType
                driverType = Regex("driverType:\\s*(\\d)")
                    .find(driverIdBlock)
                    ?.groupValues?.get(1)
                    ?: "0"

                if (prefs?.getInt(IS_NOTIFICATION)==1){
                    createNotification(title, msg, pushType.toString())}
            }

            else->{
                if (prefs?.getInt(IS_NOTIFICATION)==1){
                createNotification(title, msg, pushType.toString())}

            }
        }
    }

    private fun sendBroadcastForScreenRefresh() {
        //sending broadcast to other screens for refreshing api when notification receive
        val i = Intent("msg") //action: "msg"
        i.setPackage(packageName)
        applicationContext.sendBroadcast(i)
    }

    private fun createNotification(title: String, message: String, pushType: String) {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        CHANNEL_ID = applicationContext.packageName

        var intent: Intent? = null
        when (pushType) {
            "1" -> {
                intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userImage", senderImage)
                    putExtra("receiverId", senderId)
                    putExtra("firstName", firstName)
                    putExtra("lastName", lastName)
                    putExtra("type", "push")
                }

            }

            "2" -> {
                intent = Intent(this, UserHistoryDetailsActivity::class.java)
                    .apply {
                        putExtra("bookingId", bookingId.toString())
                        putExtra("driverType", driverType)
                    }


            }

            else -> {
                intent = Intent(this, MainActivity::class.java)
            }

        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importanceHigh = NotificationManager.IMPORTANCE_HIGH
            notificationChannel = NotificationChannel(CHANNEL_ID, "Channel One", importanceHigh)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setShowBadge(true)
            notificationChannel.description = message
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val now = Date()
        val uniqueId = now.time
        resultIntent = PendingIntent.getActivity(
            this,
            uniqueId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val icon1 = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val mNotificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setLargeIcon(icon1)
            .setContentTitle(title)
            .setOngoing(false)
            .setContentText(message)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.black))
            .setSound(sound)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(resultIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationBuilder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(uniqueId.toInt(), mNotificationBuilder.build())
    }

    private val notificationIcon: Int
        get() {
            val useWhiteIcon = true
            return if (useWhiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
        }
}
