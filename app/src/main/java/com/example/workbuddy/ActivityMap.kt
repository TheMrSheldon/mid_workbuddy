package com.example.workbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workbuddy.databinding.ActivityMainBinding

class ActivityMap : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val button = findViewById<ImageButton>(R.id.return_button)
        button.setOnClickListener {
            openMainActivity()
        }
    }

    fun openMainActivity() {
        Toast.makeText(this@ActivityMap, "You clicked me.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityMap, MainActivity::class.java)
        startActivity(intent)
    }
}