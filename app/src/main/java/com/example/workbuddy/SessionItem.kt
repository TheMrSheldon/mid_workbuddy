package com.example.workbuddy

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SessionItem(var filename: String) {
    var name: String = filename.split("_")[0]
    var timestamp: String = filename.split("_")[1]
    var date: Date = SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp);
    var datetime: String = DateFormat.format("dd.MM.yy, HH:mm", date).toString()
}