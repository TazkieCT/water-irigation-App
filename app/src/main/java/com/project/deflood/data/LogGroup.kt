package com.project.deflood.data

data class LogGroup(
    val pump: Int,
    val startTime: String,
    val endTime: String,
    val logs: List<LogEntry>
)

