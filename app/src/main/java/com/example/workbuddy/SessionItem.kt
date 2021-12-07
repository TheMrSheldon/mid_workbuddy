package com.example.workbuddy

class SessionItem(xml: String) {

    var name: String? = null
    var timestamp: String? = null

    init {
        // TODO: Load XML file
        // TODO: Set name and timestamp
        name = "Demo session name"
    }

    fun getDate(): String? {
        return name
    }

    fun getDatetime(): String {
        // TODO: Convert timestamp to readable format
        return "Demo date";
    }

}