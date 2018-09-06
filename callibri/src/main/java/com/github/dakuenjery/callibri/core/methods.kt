package com.github.dakuenjery.callibri.core

import android.os.Bundle
import android.os.Parcel
import android.os.ResultReceiver
import java.lang.reflect.Method

abstract class RemoteMethodWrapper<R>(val methodId: Int) {
    abstract fun setArguments(bundle: Bundle)
    abstract fun parseResponse(bundle: Bundle): R
}

class LocalMethodWrapper(val methodId: Int, val obj: Any, val method: Method) {
    fun handle(receiver: ResultReceiver, args: Bundle) = method.invoke(obj, receiver, args)
}



abstract class PrimitiveArgsRemoteSerializer<R> {
    abstract fun setArguments(bundle: Bundle, vararg args: Any)
    abstract fun parseResponse(bundle: Bundle): R
}

abstract class PrimitiveArgsLocalSerializer {
    abstract fun getArguments(bundle: Bundle): Array<Any>
}


//class PrimitiveArgsRemoteSerializerTest : PrimitiveArgsRemoteSerializer<Int>() {
//    override fun setArguments(bundle: Bundle, vararg args: Any) {
//        bundle.putInt()
//    }
//
//    override fun parseResponse(bundle: Bundle): Int {
//        bundle.getString()
//    }
//}