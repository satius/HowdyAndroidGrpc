package com.github.sugiyamas.howdygrpc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel


abstract class ScopedActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        (job + Dispatchers.Default).cancel()
    }
}