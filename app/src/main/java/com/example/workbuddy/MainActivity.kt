package com.example.workbuddy

import android.widget.AdapterView.OnItemClickListener
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.DialogInterface
import android.widget.EditText
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()

        // display past sessions
        val sessions: ArrayList<SessionItem> = getSessionData()
        val activityListView: ListView? = findViewById(R.id.session_list)
        activityListView!!.adapter = CustomListAdapter(this, sessions)
        activityListView.onItemClickListener =
            OnItemClickListener { a, v, position, id ->
                val o = activityListView.getItemAtPosition(position)
                val session: SessionItem = o as SessionItem
                // TODO: Open ActivityMap with name session.name
                // session.name
            }

        // make session runnable
        val startMeetingButton = findViewById<Button>(R.id.StartMeeting)
        startMeetingButton.setOnClickListener {
            showSessionNamePrompt()
        }
    }

    private fun showSessionNamePrompt() {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Meeting name")
        val input = EditText(this)
        alert.setView(input)
        alert.setPositiveButton("Start meeting", DialogInterface.OnClickListener { dialog, whichButton ->
            openCallViewActivity(input.text.toString())
        })
        alert.setNegativeButton("Cancel") { _, _ -> {} }
        alert.show()
    }

    private fun getSessionData(): ArrayList<SessionItem> {
        val output: ArrayList<SessionItem> = ArrayList<SessionItem>()

        // TODO: For all XML files create a new session and add it to output

        // EXAMPLE DATA:
        val session1: SessionItem = SessionItem("test")
        val session2: SessionItem = SessionItem("test")
        output.add(session1)
        output.add(session2)

        return output;
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
