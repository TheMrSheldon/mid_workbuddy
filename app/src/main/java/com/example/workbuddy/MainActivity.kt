package com.example.workbuddy

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workbuddy.databinding.ActivityMainBinding

import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.internal.ContextUtils.getActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var activityListView: ListView? = null
    private var arrayAdapter: ArrayAdapter<*>? = null
    private lateinit var exampleActivities: Array<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        activityListView = findViewById(R.id.months_list);
        exampleActivities = getResources().getStringArray(R.array.array_example_activities);
        arrayAdapter = ArrayAdapter<Any?>(this, android.R.layout.simple_list_item_1, exampleActivities)

        // create session name for new session
        var newSession = "session1"
        if ((arrayAdapter as ArrayAdapter<*>).count != 0) {
            val sessionName = (arrayAdapter as ArrayAdapter<*>).getItem(
                (arrayAdapter as ArrayAdapter<*>).count - 1)
            newSession = getSessionName(sessionName as String)
        }
        activityListView?.adapter = arrayAdapter
        // make session runable
        val button = findViewById<Button>(R.id.StartMeeting)
        button.setOnClickListener {
            openCallViewActivity(newSession)
        }

        // TODO make old sessions viewable
    }

    fun getSessionName(lastSession: String): String {
        val num = lastSession.replace("[^0-9]".toRegex(), "")
        return  "session" + Integer.parseInt(num) + 1

    }

    fun openMapActivity() {
        val intent = Intent(this@MainActivity, ActivityMap::class.java)
        startActivity(intent)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    fun openCallViewActivity(sessionNumber: String) {
        Toast.makeText(this@MainActivity, "Call started", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, ActivityCallView::class.java)
        intent.putExtra("session", sessionNumber)
        startActivity(intent)
    }
}


