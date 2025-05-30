package com.live.pastransport.home.activity

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.live.pastransport.R
import com.live.pastransport.adapter.ChatAdapter
import com.live.pastransport.auth.activity.ChooseUserTypeActivity
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.MyApplication.Companion.prefs
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.databinding.ActivityChatBinding
import com.live.pastransport.databinding.DialogReportBinding
import com.live.pastransport.databinding.MenuLayoutBinding
import com.live.pastransport.databinding.ReportBottomSheetBinding
import com.live.pastransport.responseModel.ChatModel
import com.live.pastransport.responseModel.MessagesModel
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.utils.Utils
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.loadImageFromServer
import com.live.pastransport.utils.visible
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class ChatActivity : AppCompatActivity(), SocketManager.Observer {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private val list = ArrayList<MessagesModel.Getdata>()

    private var isBlocked = "0"
    private var isBlockedByOther = ""
    private var type = ""

    private var userImage = ""
    private var receiverId = ""
    private var firstName = ""
    private var lastName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!socketManager.isConnected()) {
            socketManager.init()
        }

        userImage = intent.getStringExtra("userImage").toString()
        receiverId = intent.getStringExtra("receiverId").toString()
        firstName = intent.getStringExtra("firstName").toString()
        lastName = intent.getStringExtra("lastName").toString()
        type = intent.getStringExtra("type").toString()

        socketManager.setupOneToOneChatListener()
        socketManager.setupReadUnreadListener()
        socketManager.setupSendMessageListener()
        socketManager.setupReportUserListener()
        socketManager.setupClearChatListener()
        socketManager.setupBlockUnblockListener()

        setClickListeners()

        getMessageList()
        readUnreadStatus()
        setAdapter()
    }

    private fun getMessageList() {
        val jsonObject = JSONObject()
        jsonObject.put("senderId", getPrefrence("UserId", "").toString())
        jsonObject.put("receiverId", receiverId)

        socketManager.emitEvent(SocketManager.GET_MESSAGES_EMIT, jsonObject)
    }

    private fun readUnreadStatus() {
        val jsonObject = JSONObject()
        jsonObject.put("senderId", getPrefrence("UserId", "").toString())
        jsonObject.put("receiverId", receiverId)

        socketManager.emitEvent(SocketManager.READ_UNREAD_EMIT, jsonObject)
    }

    private fun setAdapter() {
        adapter = ChatAdapter(this, list)
        binding.rvChat.adapter = adapter
    }

    private fun setClickListeners() {
        with(binding) {
            ivProfile.loadImageFromServer(this@ChatActivity, userImage)
            tvHeading.text = "$firstName $lastName"

            ivBack.setOnClickListener {
                if (type=="null"){
                  finish()
                }
                else{
                    val intent = Intent(this@ChatActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

                     startActivity(intent)
                }
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
            ivMenu.setOnClickListener {
                showPopUp(it)
            }

            btnSend.setOnClickListener {
                if (binding.etMessage.text.toString().trim().isEmpty()) {
                    Utils.errorAlert(this@ChatActivity, getString(R.string.pls_enter_message))
                    return@setOnClickListener
                } else if (isBlocked == "1") {
                    Utils.errorAlert(this@ChatActivity,
                        getString(R.string.pls_unblock_this_user_before_msg))
                    return@setOnClickListener
                }  else if (isBlockedByOther == "1") {
                    Utils.errorAlert(this@ChatActivity,
                        "You have been blocked by $firstName")
                    return@setOnClickListener
                }
                else if (binding.etMessage.text.toString().isNotEmpty()) {
                    sendMessage(1, binding.etMessage.text.toString())
                    binding.etMessage.setText("")
                }
            }
        }
    }


    private fun sendMessage(type: Int, message: String) {
        val jsonObject = JSONObject()
        jsonObject.put("senderId",  getPrefrence("UserId", "").toString())
        jsonObject.put("receiverId", receiverId)
        jsonObject.put("message", message)
        jsonObject.put("message_type", type)


        socketManager.emitEvent(SocketManager.SEND_MESSAGE_EMIT, jsonObject)
    }


    private fun showPopUp(view: View) {
        val binding = MenuLayoutBinding.inflate(LayoutInflater.from(this))

        val popupWindow = PopupWindow(binding.root, ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        when(isBlocked){
            "0" -> {
                binding.tvBlock.text = "Block"
            }
            "1" ->{
                binding.tvBlock.text = "Unblock"
            }
        }

        binding.tvReport.setOnClickListener {
            showDialog()
            popupWindow.dismiss()
        }
        binding.tvClearChat.setOnClickListener {
            showClearChatDialog()
            popupWindow.dismiss()
        }
        binding.tvBlock.setOnClickListener {
            showBlockDialog()
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true
        binding.root.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(view)
    }

    private fun showDialog() {
        val dialogBinding = ReportBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogBinding.btnYes.setOnClickListener {
            dialog.dismiss()
            showReportDialog()
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showClearChatDialog() {
        val dialogBinding = ReportBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogBinding.ivTop.setImageResource(R.drawable.spam)
        dialogBinding.tvHeading.text = getString(R.string.are_you_sure_you_want_to_clear_this_chat)
        dialogBinding.btnYes.setOnClickListener {
            dialog.dismiss()

            val jsonObjects = JSONObject()
            jsonObjects.put("senderId", getPrefrence("UserId", "").toString())
            jsonObjects.put("receiverId", receiverId)

            Log.d("Socket", jsonObjects.toString())
            socketManager.emitEvent(SocketManager.CLEAR_CHAT_EMIT, jsonObjects)
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBlockDialog() {
        val dialogBinding = ReportBottomSheetBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialogBinding.ivTop.setImageResource(R.drawable.icon_delete)

        when(isBlocked){
            "0" -> {
                dialogBinding.tvHeading.text = getString(R.string.are_you_sure_you_want_to_block_this_user)
            }
            "1" ->{
                dialogBinding.tvHeading.text = getString(R.string.are_you_sure_you_want_to_unblock_this_user)
            }
        }

        dialogBinding.btnYes.setOnClickListener {
            dialog.dismiss()

            val jsonObjects = JSONObject()
            jsonObjects.put("senderId", getPrefrence("UserId", "").toString())
            jsonObjects.put("receiverId", receiverId)

            if (isBlocked == "1"){
                jsonObjects.put("status", "0")
            } else {
                jsonObjects.put("status", "1")
            }

            socketManager.emitEvent(SocketManager.BLOCK_UNBLOCK_EMIT, jsonObjects)
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showReportDialog() {
        val dialogBinding = DialogReportBinding.inflate(layoutInflater)
        val dialog = Dialog(this)

        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val params = dialog.window?.attributes
        params?.gravity = Gravity.BOTTOM
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = params

        dialogBinding.btnSubmit.setOnClickListener {
            dialog.dismiss()

            if (dialogBinding.etReportMsg.text.toString().trim().isNotEmpty()){
                val jsonObjects = JSONObject()
                jsonObjects.put("senderId", getPrefrence("UserId", "").toString())
                jsonObjects.put("receiverId", receiverId)
                jsonObjects.put("message", dialogBinding.etReportMsg.text.toString().trim())

                socketManager.emitEvent(SocketManager.REPORT_USER_EMIT, jsonObjects)
            }
        }
        dialog.show()
    }

    override fun onResponseArray(event: String, args: JSONArray) {
    }

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {
            SocketManager.GET_MESSAGES_LISTENER -> {
                runOnUiThread {
                    try {
                        if (args != null) {
                            val model = Gson().fromJson(args.toString(), MessagesModel::class.java)

                            if (model != null) {
                                isBlockedByOther = args.optString("blockByHim").toString()
                                isBlocked = args.optString("blockByMe").toString()
                            }

                            list.clear()
                            if (!model.getdata.isNullOrEmpty()) {
                                binding.rvChat.visible()
                                binding.tvNoDataFound.gone()
                                list.addAll(model.getdata)
                                adapter.notifyDataSetChanged()
                                binding.rvChat.scrollToPosition(list.size - 1)
                            } else {
                                binding.rvChat.gone()
                                binding.tvNoDataFound.visible()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(ContentValues.TAG, "onResponse: " + e.message)
                    }
                }
            }

            SocketManager.SEND_MESSAGE_LISTENER -> {
                runOnUiThread {
                    try {
                        if (args != null) {
                            binding.rvChat.visible()
                            binding.tvNoDataFound.gone()
                            val model = Gson().fromJson(args.toString(), ChatModel::class.java)

                            val newModel = MessagesModel.Getdata()
                            newModel.senderId = MessagesModel.Getdata.SenderId(
                                _id = model.senderId?._id.toString(),
                                image = model.senderId?.image.toString())

                            newModel.receiverId = MessagesModel.Getdata.ReceiverId(
                                _id = model.receiverId?._id.toString(),
                                image = model.receiverId?.image.toString())
                            if (receiverId == newModel.senderId!!._id ||
                                getPrefrence("UserId", "") == newModel.senderId!!._id) {

                                newModel.message = model.message
                                newModel.createdAt = model.createdAt
                                list.add(newModel)

                                // Notify only the newly added item
                                adapter.notifyItemInserted(list.size - 1)

                                // Scroll to the latest message
                                binding.rvChat.scrollToPosition(list.size - 1)
                            }



                        }
                    } catch (e: Exception) {
                        Log.d(ContentValues.TAG, "onResponse: " + e.message)
                    }
                }
            }

            SocketManager.REPORT_USER_LISTENER -> {
                runOnUiThread {
                    Utils.showToast(this, "User Reported Successfully")
                }
            }

            SocketManager.CLEAR_CHAT_LISTENER -> {
                lifecycleScope.launch {
                    list.clear()
                    adapter.notifyDataSetChanged()

                    binding.rvChat.gone()
                    binding.tvNoDataFound.visible()
                }
            }

            SocketManager.BLOCK_UNBLOCK_LISTENER -> {
                lifecycleScope.launch {
                    if (args.has("get_data")){
                        val getData = args.getJSONObject("get_data")
                        val senderId = getData.optString("senderId")

                        isBlocked = args.optString("blockByMe")
                        isBlockedByOther = args.optString("blockByHim")
                        val msg = args.optString("message")

                        if (senderId == getPrefrence("UserId", "").toString()){
                            Utils.showToast(this@ChatActivity, msg)
                        }
                    }
                }
            }
        }
    }

    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }

    override fun onResume() {
        super.onResume()
        prefs?.saveString("STATUS_CHAT", "true$receiverId")

        socketManager.registerObserver(this)
    }

    override fun onStop() {
        super.onStop()
        prefs?.saveString("STATUS_CHAT", "false")

    }
    override fun onDestroy() {
        super.onDestroy()
        prefs?.saveString("STATUS_CHAT", "false")

        socketManager.unregisterObserver(this)
    }
}