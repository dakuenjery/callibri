package com.github.dakuenjery.callibri.core

import android.os.*

interface Call<R> {
    fun onSuccess(f: (R) -> Unit): Call<R>
    fun onError(f: (Exception) -> Unit): Call<R>
}

open class CallImpl<R>(private val handler: Handler) : Call<R> {
    private var data: R? = null
    private var err: Exception? = null

    private var successCallback: ((R) -> Unit)? = null
    private var errorCallback: ((Exception) -> Unit)? = null

    fun setData(data: R) {
        doubleUsageCheck()

        this.data = data

        successCallback?.let {
            handler.post { it.invoke(data) }
        }
    }

    fun setError(ex: Exception) {
        doubleUsageCheck()

        this.err = ex

        errorCallback?.let {
            handler.post { it.invoke(ex) }
        }
    }

    override fun onSuccess(f: (R) -> Unit): Call<R> {
        successCallback = f
        data?.let {
            handler.post { f.invoke(it) }
        }
        return this
    }

    override fun onError(f: (Exception) -> Unit): Call<R> {
        errorCallback = f
        err?.let {
            handler.post { f.invoke(it) }
        }
        return this
    }

    private fun doubleUsageCheck() {
        if (this.data != null || err != null)
            throw Exception("Double usage!")
    }
}

abstract class ParsableMethodCall<R>(responseHandler: Handler) : CallImpl<R>(responseHandler) {
    abstract fun handleBundle(bundle: Bundle): R

    fun handleAnswer(msg: Message) {
        when (msg?.what) {
            RESPONSE_MSG_ID -> {
                try {
                    val r = handleBundle(msg.data)
                    setData(r)
                } catch (ex: Exception) {
                    setError(Exception("Parse error"))
                }
            }
            else -> setError(Exception("Unknown error"))
        }
    }
}

internal class MethodCall<R>(responseHandler: Handler, val serializerPrimitive: PrimitiveArgsRemoteSerializer<R>) : ParsableMethodCall<R>(responseHandler) {
    override fun handleBundle(bundle: Bundle): R = serializerPrimitive.parseResponse(bundle)
}

