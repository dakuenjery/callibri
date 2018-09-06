package com.github.dakuenjery.callibri.core

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.ResultReceiver


open class Invoke(msg: Message, val serviceId: Int) : ResultReceiver(null) {
    private val requestId: Int = msg.arg1
    private val data: Bundle = msg.data
    private val replyTo: Messenger = msg.replyTo

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        val msg = Message.obtain(null, RESPONSE_MSG_ID, requestId, serviceId)
        msg.data = resultData
        replyTo.send(msg)
    }
}