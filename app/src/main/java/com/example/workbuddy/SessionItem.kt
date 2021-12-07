package com.example.workbuddy

class SessionItem(filename: String) {

    lateinit var filename: String
    lateinit var name: String
    lateinit var timestamp: String

    init {
        this.filename = filename
        name = filename.split("_")[0]
        timestamp = filename.split("_")[1]
    }
}