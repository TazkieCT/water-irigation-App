package com.project.deflood.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.project.deflood.data.RabbitRepository

class RabbitService : Service() {

    private val repo = RabbitRepository()

    companion object {
        const val ACTION_UPDATE_UI = "com.project.deflood.UPDATE_UI"
        const val EXTRA_DATA = "extra_data"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        repo.start { msg ->
            val broadcastIntent = Intent(ACTION_UPDATE_UI)
            broadcastIntent.putExtra(EXTRA_DATA, msg)
            sendBroadcast(broadcastIntent)
        }
        return START_STICKY
    }

    fun sendPumpCommand(pumpId: Int, isOn: Boolean) {
        repo.sendCommand(pumpId, isOn)
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): RabbitService = this@RabbitService
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder()
    }
}
