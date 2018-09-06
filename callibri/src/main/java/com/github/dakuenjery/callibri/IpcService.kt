package com.github.dakuenjery.callibri

import android.os.*
import com.github.dakuenjery.callibri.annotations.LocalMethod
import com.github.dakuenjery.callibri.core.CallibriHandler
import com.github.dakuenjery.callibri.core.Invoke
import com.github.dakuenjery.callibri.core.LocalMethodWrapper
import com.github.dakuenjery.callibri.core.PrimitiveArgsLocalSerializer

interface LocalSerializerFactory {
    fun createSerializer(methodId: Int): PrimitiveArgsLocalSerializer
}

abstract class IpcService {

    protected abstract val serializerFactory: LocalSerializerFactory

    private val handler = object : CallibriHandler() {
        override fun handleMessage(msg: Message?) {
            val args = serializerFactory.createSerializer(msg!!.what).getArguments(msg.data)
            handleMessage(msg.what, Invoke(msg, hashCode()), *args)
        }
    }

    protected abstract fun handleMessage(msgId: Int, receiver: ResultReceiver, vararg args: Any)

//    private val localMethodsWrapper: Map<Int, LocalMethodWrapper> by lazy {
//        val methods = this::class.java.methods
//                .filter {
//                    it.annotations.any { it is LocalMethod }
//                }.map {
//                    val annotation = it.annotations.find { it is LocalMethod } as LocalMethod
//
//                    if (annotation.methodId < 1000)
//                        throw Exception("Method id must be >= 1000")
//
//                    LocalMethodWrapper(annotation.methodId, this, it)
//                }.associateBy { it.methodId }
//
//        return@lazy methods
//
//        return@lazy emptyMap<Int, LocalMethodWrapper>()
//    }



    val binder: IBinder get() = handler.messenger.binder
}

//interface