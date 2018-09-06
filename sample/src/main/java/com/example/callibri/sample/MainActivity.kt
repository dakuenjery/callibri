package com.example.callibri.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import callibri.generated.ClientIpcImpl
import com.github.dakuenjery.callibri.IpcClient
import com.github.dakuenjery.callibri.annotations.RemoteMethod
import com.github.dakuenjery.callibri.core.Call


abstract class ClientIpc : IpcClient() {
    @RemoteMethod(1001)
    abstract fun test(arg1: Int, arg2: kotlin.String): Call<String>

    @RemoteMethod(1002)
    abstract fun test2(arg: String): Call<Double>
}


class MainActivity : AppCompatActivity() {

    val ipc = ClientIpcImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ipc.connect(Intent("callibri.Service").apply { setPackage("com.example.callibri.sample") }, this)
                .onSuccess { Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show() }
                .onError { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
    }

    fun invokeRemoteMethod(view: View) {
        ipc.test(2, "Hello")
                .onSuccess {
                    Toast.makeText(this, "Response: $it", Toast.LENGTH_SHORT).show()

                    ipc.test2("12.3+33.4")
                            .onSuccess { Toast.makeText(this, "Response: $it", Toast.LENGTH_SHORT).show() }
                            .onError { Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show() }
                }
                .onError {
                    Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
                }

    }
}
