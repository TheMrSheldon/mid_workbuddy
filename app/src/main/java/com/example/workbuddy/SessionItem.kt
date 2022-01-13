package com.example.workbuddy

import android.net.Uri
import android.text.format.DateFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SessionItem(filename: String) {
    lateinit var filename: String
    lateinit var name: String
    lateinit var timestamp: String
    lateinit var date: String
    lateinit var time: String

    init {
        this.filename = filename
        val splits = filename.split("_")
        this.name = filename.dropLast(splits[splits.size -1].length + 1)
        this.timestamp = splits[splits.size - 1]
        val duration = timestamp.substring(timestamp.lastIndexOf('#') + 1)
        this.timestamp = timestamp.substring(0, timestamp.lastIndexOf('#'))
        val date: Date = SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp)
        this.date = DateFormat.format("dd.MM.yy", date).toString()
        this.time = DateFormat.format("HH:mm", date).toString() + " (" + duration + " Minutes)" // TODO: Get animation
    }
}