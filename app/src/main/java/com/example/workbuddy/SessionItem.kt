package com.example.workbuddy

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SessionItem(filename: String) {
    lateinit var filename: String
    lateinit var name: String
    lateinit var timestamp: String
    lateinit var datetime: String

    init {
        this.filename = filename
        this.name = filename.split("_")[0]
        this.timestamp = filename.split("_")[1]
        val date: Date = SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp)
        this.datetime = DateFormat.format("dd.MM.yy, HH:mm", date).toString()
    }
}