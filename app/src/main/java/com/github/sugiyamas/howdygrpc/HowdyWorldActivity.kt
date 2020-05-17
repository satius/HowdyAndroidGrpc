package com.github.sugiyamas.howdygrpc

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.github.sugiyamas.grpc.howdy.GreeterGrpcKt
import com.github.sugiyamas.grpc.howdy.HowdyRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HowdyWorldActivity : ScopedActivity() {

    private val sendButton by lazy { findViewById<Button>(R.id.send_button) }
    private val hostEdit by lazy { findViewById<EditText>(R.id.host_edit_text) }
    private val portEdit by lazy { findViewById<EditText>(R.id.port_edit_text) }
    private val messageEdit by lazy { findViewById<EditText>(R.id.message_edit_text) }
    private val resultText by lazy { findViewById<TextView>(R.id.grpc_response_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howdyworld)
        resultText.movementMethod = ScrollingMovementMethod()
    }

    fun sendMessage(view: View?) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(hostEdit.windowToken, 0)
        sendButton.isEnabled = false
        resultText.text = ""

        val port = portEdit.text.toString().toIntOrNull() ?: return
        val host = hostEdit.text.toString()
        val message = messageEdit.text.toString()
        val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
        launch { requestSayHowdy(channel, message) }
    }

    private suspend fun requestSayHowdy(channel: ManagedChannel, message: String) {
        val stub = GreeterGrpcKt.GreeterCoroutineStub(channel)
        val request = HowdyRequest.newBuilder()
            .setName(message)
            .build()

        try {
            val response = withContext(Dispatchers.Default) {
                stub.sayHowdy(request)
            }
            finalize(channel, response.message)
        } catch (e: Exception) {
            e.printStackTrace()
            finalize(channel, e.message)
        }
    }

    private fun finalize(channel: ManagedChannel, resultStr: String?) {
        resultStr?.let { resultText.text = it }
        sendButton.isEnabled = true
        channel.shutdown()
    }

}
