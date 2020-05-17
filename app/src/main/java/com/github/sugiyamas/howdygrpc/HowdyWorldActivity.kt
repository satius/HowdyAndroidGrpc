package com.github.sugiyamas.howdygrpc

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.github.sugiyamas.grpc.howdy.GreeterGrpcKt
import com.github.sugiyamas.grpc.howdy.HowdyRequest
import com.github.sugiyamas.howdygrpc.databinding.ActivityHowdyworldBinding
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HowdyWorldActivity : ScopedActivity(), HowdyWorldView {

    private lateinit var binding: ActivityHowdyworldBinding
    override val vm: HowdyWorldViewModel = HowdyWorldViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityHowdyworldBinding>(
            this,
            R.layout.activity_howdyworld
        ).also {
            it.vm = vm
            it.handlers = this
            it.grpcResponseText.movementMethod = ScrollingMovementMethod()
        }
    }

    override fun onRequestButtonClick(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.hostEditText.windowToken, 0)
        binding.sendButton.isEnabled = false
        binding.grpcResponseText.text = ""

        val port = binding.portEditText.text.toString().toIntOrNull() ?: return
        val host = binding.hostEditText.text.toString()
        val message = binding.messageEditText.text.toString()
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
        resultStr?.let { binding.grpcResponseText.text = it }
        binding.sendButton.isEnabled = true
        channel.shutdown()
    }

}
