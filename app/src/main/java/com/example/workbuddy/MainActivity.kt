package com.example.workbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workbuddy.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.StartMeeting)
        button.setOnClickListener {
            openCallViewActivity()
        }
    }

    fun openMapActivity() {
        val intent = Intent(this@MainActivity, ActivityMap::class.java)
        startActivity(intent)
    }
    fun openCallViewActivity() {
        Toast.makeText(this@MainActivity, "Call started", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, ActivityCallView::class.java)
        startActivity(intent)
    }
}


