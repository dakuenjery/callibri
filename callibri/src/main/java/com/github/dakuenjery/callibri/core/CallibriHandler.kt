package com.github.dakuenjery.callibri.core

import android.os.*

internal abstract class CallibriHandler {
    val handlerThread = HandlerThread("CallibriThread").apply {
        start()
    }

    inner class IpcHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            try {
                this@CallibriHandler.handleMessage(msg)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    val ioHandler = IpcHandler(handlerThread.looper)
    val messenger = Messenger(ioHandler)

    protected abstract fun handleMessage(msg: Message?)
}