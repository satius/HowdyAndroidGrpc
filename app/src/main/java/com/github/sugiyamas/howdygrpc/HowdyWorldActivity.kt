package com.github.sugiyamas.howdygrpc

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.sugiyamas.grpc.howdy.GreeterGrpc
import com.github.sugiyamas.grpc.howdy.HowdyRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class HowdyWorldActivity : AppCompatActivity() {

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
        GrpcTask(this).execute(
            hostEdit.text.toString(),
            messageEdit.text.toString(),
            portEdit.text.toString()
        )
    }

    companion object {

        private class GrpcTask constructor(activity: Activity) :
            AsyncTask<String?, Void?, String>() {
            private val activityReference: WeakReference<Activity> = WeakReference(activity)
            private var channel: ManagedChannel? = null

            override fun doInBackground(vararg params: String?): String {
                val host = params[0]
                val message = params[1]
                val portStr = params[2]
                val port = portStr?.toIntOrNull() ?: 0
                return try {
                    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
                    val stub = GreeterGrpc.newBlockingStub(channel)
                    val request =
                        HowdyRequest.newBuilder().setName(message).build()
                    val reply = stub.sayHowdy(request)
                    reply.message
                } catch (e: Exception) {
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    e.printStackTrace(pw)
                    pw.flush()
                    String.format("Failed... : %n%s", sw)
                }
            }

            override fun onPostExecute(result: String) {
                try {
                    channel!!.shutdown().awaitTermination(1, TimeUnit.SECONDS)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                val activity = activityReference.get() ?: return
                val resultText =
                    activity.findViewById<View>(R.id.grpc_response_text) as TextView
                val sendButton =
                    activity.findViewById<View>(R.id.send_button) as Button
                resultText.text = result
                sendButton.isEnabled = true
            }
        }
    }

}
