package com.example.workbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ActivityCallView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_view)
        val button = findViewById<MaterialButton>(R.id.mute_button)
        button.setOnClickListener {
            openMapActivity()
        }
    }

    fun openMainActivity() {
        Toast.makeText(this@ActivityCallView, "You clicked me.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityCallView, MainActivity::class.java)
        startActivity(intent)
    }
    fun openMapActivity() {
        Toast.makeText(this@ActivityCallView, "You clicked me.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityCallView, ActivityMap::class.java)
        startActivity(intent)
    }
}