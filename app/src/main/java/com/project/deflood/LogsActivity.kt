package com.project.deflood

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.deflood.data.LogEntry

class LogsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LogsAdapter
    private lateinit var backButton: Button
    private val database = FirebaseDatabase.getInstance("https://defloodiot-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val logsRef = database.getReference("Riwayat")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        recyclerView = findViewById(R.id.recyclerViewLogs)
        backButton = findViewById(R.id.backButton)
        adapter = LogsAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            finish()
        }

        loadLogs()
    }

    private fun loadLogs() {
        logsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logs = mutableListOf<LogEntry>()
                for (child in snapshot.children) {
                    val waktu = child.child("waktu").getValue(String::class.java) ?: ""
                    val levelA = child.child("levelA").getValue(Double::class.java) ?: 0.0
                    val levelB = child.child("levelB").getValue(Double::class.java) ?: 0.0
                    val pump = child.child("pump").getValue(Int::class.java) ?: 0
                    logs.add(LogEntry(waktu, levelA, levelB, pump))
                }
                adapter.updateLogs(logs)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}