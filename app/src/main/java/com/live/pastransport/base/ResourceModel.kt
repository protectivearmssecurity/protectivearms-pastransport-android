package com.live.pastransport.base

import com.google.gson.JsonElement
import com.live.pastransport.network.StatusType

//  used for returning response to UI
class ResourceModel(var key:String="", var obj:JsonElement?=null, var status: StatusType, var message:String="" )