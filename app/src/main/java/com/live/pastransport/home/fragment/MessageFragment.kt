package com.live.pastransport.home.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.live.pastransport.adapter.MessageAdapter
import com.live.pastransport.base.MyApplication
import com.live.pastransport.base.MyApplication.Companion.socketManager
import com.live.pastransport.databinding.FragmentMessageBinding
import com.live.pastransport.responseModel.MessageInboxListResponse
import com.live.pastransport.sockets.SocketManager
import com.live.pastransport.sockets.SocketManager.Companion.GET_USERS_CHAT_LIST_EMIT
import com.live.pastransport.utils.CustomProgressDialog
import com.live.pastransport.utils.getPrefrence
import com.live.pastransport.utils.gone
import com.live.pastransport.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class MessageFragment : Fragment(), SocketManager.Observer {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var messageAdapter: MessageAdapter
    private val progressDialog by lazy {
        CustomProgressDialog(Dialog(requireActivity()!!))
    }
    private val list = ArrayList<MessageInboxListResponse.Getdata>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog.show(requireActivity())
        if (!socketManager.isConnected()) {
            socketManager.init()
        }
        setClickListeners()
    }

    private fun getMessageList() {
        val jsonObject = JSONObject()
        jsonObject.put("senderId", getPrefrence("UserId", "").toString())
        socketManager.emitEvent(GET_USERS_CHAT_LIST_EMIT, jsonObject)
    }

    private fun setAdapter() {
        messageAdapter = MessageAdapter(requireContext(), list)
        binding.rvMessages.adapter = messageAdapter
    }

    private fun setClickListeners() {
    }

    override fun onResponseArray(event: String, args: JSONArray) {
    }

    override fun onResponse(event: String, args: JSONObject) {
        when (event) {

            SocketManager.GET_USERS_CHAT_LIST_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        progressDialog.hide()

                        list.clear()
                        val data = args as JSONObject
                        val gson = GsonBuilder().create()
                        val model =
                            gson.fromJson(data.toString(), MessageInboxListResponse::class.java)
                        val getDataList = model.getdata

                        list.addAll(getDataList)
                        setAdapter()

                        if (list.isEmpty()) {
                            binding.tvNoDataFound.visible()
                        } else {
                            binding.tvNoDataFound.gone()
                        }
                    } catch (e: Exception) {
                        Log.e("ParsingError", "Failed to parse the response", e)
                    }
                }
            }
        }
    }

    override fun onError(event: String, vararg args: Array<*>) {
    }

    override fun onBlockError(event: String, args: String) {
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unregisterObserver(this)
    }

    override fun onResume() {
        super.onResume()
        socketManager.registerObserver(this)

        lifecycleScope.launch {
            socketManager.setupMyAllTypeChatsListener()
            getMessageList()
        }
    }
}