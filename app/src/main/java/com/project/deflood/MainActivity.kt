package com.project.deflood

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.project.deflood.service.RabbitService

class MainActivity : AppCompatActivity() {
    private lateinit var statusA: TextView
    private lateinit var switchA: SwitchCompat
    private lateinit var barA: ProgressBar
    private lateinit var percentageA: TextView

    private lateinit var statusB: TextView
    private lateinit var switchB: SwitchCompat
    private lateinit var barB: ProgressBar
    private lateinit var percentageB: TextView

    private var rabbitService: RabbitService? = null
    private var isBound = false

    // Flag untuk mencegah listener aktif saat update programmatic
    private var isUpdatingFromEsp = false
    // Track mode manual untuk setiap pump
    private var manualModeA = false
    private var manualModeB = false

    private val dataReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == RabbitService.ACTION_UPDATE_UI) {
                val data = p1.getStringExtra(RabbitService.EXTRA_DATA) ?: return

                showData(data)
            }
        }
    }

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as RabbitService.LocalBinder
            rabbitService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, RabbitService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        startService(intent)

        initView()
        setupSwitchListeners()
    }

    private fun setupSwitchListeners() {
        switchA.setOnCheckedChangeListener { _, isChecked ->
            // Abaikan jika perubahan dari ESP32, bukan dari user tap
            if (isUpdatingFromEsp) return@setOnCheckedChangeListener

            if (isBound) {
                // ON = mode manual aktif, OFF = kembali ke mode otomatis
                manualModeA = isChecked
                rabbitService?.sendPumpCommand(1, isChecked)
                statusA.text = if(isChecked) "Status: Manual ON" else "Status: Otomatis"
            }
        }

        switchB.setOnCheckedChangeListener { _, isChecked ->
            // Abaikan jika perubahan dari ESP32, bukan dari user tap
            if (isUpdatingFromEsp) return@setOnCheckedChangeListener

            if (isBound) {
                // ON = mode manual aktif, OFF = kembali ke mode otomatis
                manualModeB = isChecked
                rabbitService?.sendPumpCommand(2, isChecked)
                statusB.text = if(isChecked) "Status: Manual ON" else "Status: Otomatis"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(RabbitService.ACTION_UPDATE_UI)
        registerReceiver(dataReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataReceiver)
    }

    private fun initView() {
        statusA = findViewById(R.id.waduk1_status)
        switchA = findViewById(R.id.waduk1_switch)
        barA = findViewById(R.id.waduk1_capacity)
        percentageA = findViewById(R.id.waduk1_percent)

        statusB = findViewById(R.id.waduk2_status)
        switchB = findViewById(R.id.waduk2_switch)
        barB = findViewById(R.id.waduk2_capacity)
        percentageB = findViewById(R.id.waduk2_percent)
    }

    private fun showData(data: String) {
        try {
            val parts = data.split("|")

            val pumpStatus = parts[0].split(":")[1].trim()
            val levelA = parts[1].split(":")[1].trim().toDouble() // Jarak sensor A
            val levelB = parts[2].split(":")[1].trim().toDouble() // Jarak sensor B

            runOnUiThread {
                // Set flag agar listener tidak aktif saat update programmatic
                isUpdatingFromEsp = true

                val percentA = toPercent(levelA)
                barA.progress = percentA
                percentageA.text = "$percentA%"

                // Hanya update switch & status dari ESP jika tidak dalam mode manual
                val espPumpAOn = pumpStatus.contains("1", ignoreCase = true)
                if (!manualModeA) {
                    switchA.isChecked = espPumpAOn
                    statusA.text = if (percentA > 80) "Status: BAHAYA" else "Status: Aman"
                }

                val percentB = toPercent(levelB)
                barB.progress = percentB
                percentageB.text = "$percentB%"

                val espPumpBOn = pumpStatus.contains("2", ignoreCase = true)
                if (!manualModeB) {
                    switchB.isChecked = espPumpBOn
                    statusB.text = if (percentB > 80) "Status: BAHAYA" else "Status: Aman"
                }

                // Reset flag setelah selesai update
                isUpdatingFromEsp = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toPercent(number: Double): Int {
        return (number * 100).toInt()
    }
}