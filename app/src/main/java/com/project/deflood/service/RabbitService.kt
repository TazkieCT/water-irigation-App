package com.project.deflood.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.project.deflood.data.RabbitRepository

class RabbitService : Service() {

    private val repo = RabbitRepository()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        repo.start { msg ->
            Log.d("RabbitService", "Message: $msg")
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null
}
