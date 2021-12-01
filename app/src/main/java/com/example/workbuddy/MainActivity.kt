package com.example.workbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workbuddy.databinding.ActivityMainBinding

import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var activityListView: ListView? = null
    private var arrayAdapter: ArrayAdapter<*>? = null
    private lateinit var exampleActivities: Array<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    fun openCallViewActivity(sessionNumber: String) {
        Toast.makeText(this@MainActivity, "Call started", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, ActivityCallView::class.java)
        intent.putExtra("session", sessionNumber)
        startActivity(intent)
    }
}


