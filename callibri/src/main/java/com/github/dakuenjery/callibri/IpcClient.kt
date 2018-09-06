package com.github.dakuenjery.callibri

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.github.dakuenjery.callibri.core.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

interface RemoteMethodWrapperFactory {
    fun createMethodWrapper(methodId: Int): PrimitiveArgsRemoteSerializer<*>
}

abstract class IpcClient {
    protected abstract val methodWrapperFactory: RemoteMethodWrapperFactory

    private val invokeIdGenerator = AtomicInteger(0)
    private val callMap = ConcurrentHashMap<Int, MethodCall<*>>()

    private val mainHandler = Handler(Looper.getMainLooper())

    private val handler = object : CallibriHandler() {
        override fun handleMessage(msg: Message?) {
            callMap.get(msg!!.arg1)!!.handleAnswer(msg)
        }
    }

    private val connection: Connection = Connection(null)

    fun connect(intent: Intent, context: Context): Call<Unit> {
        val call = CallImpl<Unit>(mainHandler)

        connection.call = call
        val r = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        if (!r)
            call.setError(Exception("Cannot bind service"))

        return call
    }

    protected fun <T> remoteInvoke(methodId: Int, vararg args: Any): Call<T> {
         val wrapper = methodWrapperFactory.createMethodWrapper(methodId)

        val invokeId = invokeIdGenerator.incrementAndGet()
        val msg = Message.obtain(null, methodId, invokeId, hashCode()).apply {
            data = Bundle().apply { wrapper.setArguments(this, *args) }
            replyTo = handler.messenger
        }

        val call = MethodCall(mainHandler, wrapper)

        callMap[invokeId] = call

        connection.send(msg)

        return call as Call<T>
    }
}

private class Connection(var call: CallImpl<Unit>?) : ServiceConnection {

    private var service: Messenger? = null

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        this.service = Messenger(service)

        call?.setData(Unit)
        call = null
    }

    override fun onServiceDisconnected(name: ComponentName) {
        this.service = null

        call?.setError(Exception("Disconnect from $name"))
        call = null
    }

    fun send(msg: Message) {
        service?.send(msg)
    }
}