package com.example.callibri.sample

import android.app.Service
import android.content.Intent
import android.os.*
import callibri.generated.IpcImpl
import com.github.dakuenjery.callibri.IpcService
import com.github.dakuenjery.callibri.annotations.LocalMethod


abstract class Ipc : IpcService() {

    @LocalMethod(1001)
    fun test(receiver: ResultReceiver, arg1: Int, arg2: String) {
        receiver.send(0, Bundle().apply { putString(null, "$arg1 $arg2") })
    }

    @LocalMethod(1002)
    fun test2(receiver: ResultReceiver, arg: String) {
        val splt = arg.split('+')
                .map { it.toDouble() }
                .reduce { acc, d -> acc + d }

        receiver.send(0, Bundle().apply { putDouble(null, splt) })
    }
}

class MyService : Service() {

    val ipc = IpcImpl()

    override fun onBind(intent: Intent): IBinder {
        return ipc.binder
    }
}
