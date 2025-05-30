package com.live.pastransport.sockets

import android.util.Log
import com.live.pastransport.base.SOCKET_BASE_URL
import com.live.pastransport.utils.getPrefrence
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException


class SocketManager {

    companion object {
        const val ERROR_MESSAGE = "error_message"

        private const val CONNECT_USER = "connect_user"
        const val CONNECT_LISTENER = "connect_user_listener"

        const val GET_USERS_CHAT_LIST_EMIT = "user_constant_list"
        const val GET_USERS_CHAT_LIST_LISTENER = "user_constant_chat_list"

        const val GET_MESSAGES_EMIT = "users_chat_list"
        const val GET_MESSAGES_LISTENER = "users_chat_list_listener"

        const val SEND_MESSAGE_EMIT = "send_message"
        const val SEND_MESSAGE_LISTENER = "send_message_emit"

        const val REPORT_USER_EMIT = "report_message"
        const val REPORT_USER_LISTENER = "report_message_listener"

        const val CLEAR_CHAT_EMIT = "clear_chat"
        const val CLEAR_CHAT_LISTENER = "clear_chat_listener"

        const val BLOCK_UNBLOCK_EMIT = "block_unblock_user"
        const val BLOCK_UNBLOCK_LISTENER = "block_unblock_user_listener"

        const val READ_UNREAD_EMIT = "read_unread"
        const val READ_UNREAD_LISTENER = "read_data_status"

        const val BOOKING_ACCEPT_REJECT_EMIT = "bookingAcceptReject"
        const val BOOKING_ACCEPT_REJECT_LISTENER = "bookingAcceptReject"

        const val TRACKING_EMIT = "tracking"

        const val REQUEST_SEND_TO_DRIVER_LISTENER = "requestSendToDriver"

        const val LOCATION_UPDATED_LISTENER = "locationUpdated"
    }

    private var mSocket: Socket? = null
    private val observerList: MutableList<Observer> = mutableListOf()

    fun getSocketInstance(): Socket? = mSocket

    private fun createSocket(): Socket? {
        return try {
            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                timeout = 20000
            }
            IO.socket(SOCKET_BASE_URL, options)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            null
        }
    }

    fun init() {
        if (mSocket == null) {
            mSocket = createSocket()
        }

        disconnect() // Reset before reconnect
        mSocket?.apply {
            connect()
            on(Socket.EVENT_CONNECT, onConnect)
            on(Socket.EVENT_DISCONNECT, onDisconnect)
            on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            on(ERROR_MESSAGE, onErrorMessage)
        }
    }

    fun isConnected(): Boolean = mSocket?.connected() == true

    fun registerObserver(observer: Observer) {
        if (!observerList.contains(observer)) {
            observerList.clear()
            observerList.add(observer)
        }
    }

    fun unregisterObserver(observer: Observer) {
        observerList.remove(observer)
    }

    fun disconnect() {
        mSocket?.apply {
            off()
            disconnect()
        }
    }

    private val onConnect = Emitter.Listener {
        if (isConnected()) {
            val userId = getPrefrence("UserId", "").toString()
            Log.d("dshfgvdsfdsfds",userId)
            if (userId.isNotEmpty() && userId != "0") {
                JSONObject().apply {
                    put("userId", userId)
                    mSocket?.off(CONNECT_LISTENER, onConnectListener)
                    mSocket?.on(CONNECT_LISTENER, onConnectListener)
                    mSocket?.emit(CONNECT_USER, this)
                }
            }
        }
    }

    private val onConnectListener = Emitter.Listener { args ->
        Log.e("Socket", "onConnectListener :: $args")

//        (args.firstOrNull() as? JSONObject)?.let { data ->
//            Log.e("Socket", "onConnectListener :: $data")
//            observerList.forEach { it.onResponse(CONNECT_LISTENER, data) }
//        }
    }

    private val onDisconnect = Emitter.Listener {
        if (!isConnected()) init()
    }

    private val onConnectError = Emitter.Listener {
        if (!isConnected()) init()
    }

    private val onErrorMessage = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "Error Message :: $data")
            observerList.forEach { it.onError(CONNECT_USER, args) }
        }
    }

    fun emitEvent(event: String, data: JSONObject?) {
        data?.let {
            if (!isConnected()) mSocket?.connect()
            /*  mSocket?.off(GET_USERS_CHAT_LIST_LISTENER)
              mSocket?.on(GET_USERS_CHAT_LIST_LISTENER,onGetMyAllTypeChatsListener)*/
            mSocket?.emit(event, it)
            Log.d("Socket","emitEvent called")
        }
    }

    fun setupListener(event: String, listener: Emitter.Listener) {
        try {
            if (!isConnected()) mSocket?.connect()
            mSocket?.off(event)
            mSocket?.on(event, listener)
            Log.d("Socket","setupListener called")

        } catch (ex: Exception) {
            Log.d("Socket",ex.message.toString())
            ex.printStackTrace()
        }
    }


    private val onGetMyAllTypeChatsListener = Emitter.Listener { args ->
        Log.e("Socket", "setupMyAllTypeChatsListener called"+args[0].toString())
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onGetMyAllTypeChatsListener :: $data")
            observerList.forEach { it.onResponse(GET_USERS_CHAT_LIST_LISTENER, data) }
        }
    }


    fun setupMyAllTypeChatsListener() {
        setupListener(GET_USERS_CHAT_LIST_LISTENER, onGetMyAllTypeChatsListener)
    }

    private val onGetMessagesListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onGetOneToOneChatListListener :: $data")
            observerList.forEach { it.onResponse(GET_MESSAGES_LISTENER, data) }
        }
    }

    fun setupOneToOneChatListener() {
        setupListener(GET_MESSAGES_LISTENER, onGetMessagesListener)
    }

    private val onSendMessageListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onSendMessageListener :: $data")
            observerList.forEach { it.onResponse(SEND_MESSAGE_LISTENER, data) }
        }
    }

    fun setupSendMessageListener() {
        setupListener(SEND_MESSAGE_LISTENER, onSendMessageListener)
    }

    private val onReportUserListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onReportUserListener :: $data")
            observerList.forEach { it.onResponse(REPORT_USER_LISTENER, data) }
        }
    }

    fun setupReportUserListener() {
        setupListener(REPORT_USER_LISTENER, onReportUserListener)
    }

    private val onClearChatListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onClearChatListener :: $data")
            observerList.forEach { it.onResponse(CLEAR_CHAT_LISTENER, data) }
        }
    }

    fun setupClearChatListener() {
        setupListener(CLEAR_CHAT_LISTENER, onClearChatListener)
    }

    private val onBlockUnblockListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onBlockUnblockListener :: $data")
            observerList.forEach { it.onResponse(BLOCK_UNBLOCK_LISTENER, data) }
        }
    }

    fun setupBlockUnblockListener() {
        setupListener(BLOCK_UNBLOCK_LISTENER, onBlockUnblockListener)
    }

    private val onReadUnreadListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onReadUnreadListener :: $data")
            observerList.forEach { it.onResponse(READ_UNREAD_LISTENER, data) }
        }
    }

    fun setupReadUnreadListener() {
        setupListener(READ_UNREAD_LISTENER, onReadUnreadListener)
    }
    private val onNewRequestDriverHomeListener = Emitter.Listener { args ->
        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "setupReadUnreadListener :: $data")
            observerList.forEach { it.onResponse(REQUEST_SEND_TO_DRIVER_LISTENER, data) }
        }
    }

    fun newRequestDriverHomeListener() {
        setupListener(REQUEST_SEND_TO_DRIVER_LISTENER, onNewRequestDriverHomeListener)
    }
    private val onLocationUpdateListener = Emitter.Listener { args ->
        Log.e("Socket", "onLocationUpdateListener :: $")

        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onLocationUpdateListener :: $data")
            observerList.forEach { it.onResponse(LOCATION_UPDATED_LISTENER, data) }
        }
    }

    fun locationUpdateListener() {
        setupListener(LOCATION_UPDATED_LISTENER, onLocationUpdateListener)
    }
    private val onBookingAcceptRejectListener = Emitter.Listener { args ->
        Log.e("Socket", "onBookingAcceptRejectListener :: $")

        (args.firstOrNull() as? JSONObject)?.let { data ->
            Log.e("Socket", "onBookingAcceptRejectListener :: $data")
            observerList.forEach { it.onResponse(BOOKING_ACCEPT_REJECT_LISTENER, data) }
        }
    }

    fun bookingAcceptRejectListener() {
        setupListener(BOOKING_ACCEPT_REJECT_LISTENER, onBookingAcceptRejectListener)
    }


    interface Observer {
        fun onResponseArray(event: String, args: JSONArray)
        fun onResponse(event: String, args: JSONObject)
        fun onError(event: String, vararg args: Array<*>)
        fun onBlockError(event: String, args: String)
    }
}