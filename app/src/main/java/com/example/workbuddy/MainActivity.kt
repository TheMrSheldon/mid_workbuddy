package com.example.workbuddy

import android.widget.AdapterView.OnItemClickListener
import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.widget.EditText
import androidx.core.content.ContextCompat.checkSelfPermission
import java.io.File
import android.widget.AdapterView.AdapterContextMenuInfo

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.View
import android.view.MenuItem


class MainActivity : AppCompatActivity() {
    var sessions: ArrayList<SessionItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()

        // display past sessions
        sessions = getSessionData().sortedByDescending(SessionItem::timestamp).toCollection(ArrayList())
        val activityListView = findViewById<ListView>(R.id.session_list)
        activityListView.adapter = CustomListAdapter(this, sessions)
        activityListView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val o = activityListView.getItemAtPosition(position)
                val session: SessionItem = o as SessionItem
                openMapActivity(session.filename)
            }

        // make session runnable
        val startMeetingButton = findViewById<Button>(R.id.StartMeeting)
        startMeetingButton.setOnClickListener { showSessionNamePrompt() }

        registerForContextMenu(activityListView)
    }

    private fun showSessionNamePrompt() {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Enter a name for the meeting:")
        val input = EditText(this)
        alert.setView(input)
        alert.setPositiveButton("Start meeting") { _, _ ->
            openCallViewActivity(input.text.toString()) // TODO REGEX to remove special characters (not - and _ )
        }
        alert.setNegativeButton("Cancel") { _, _ -> {} }
        alert.show()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        if (v.id == R.id.session_list) {
            menu.add(Menu.NONE, 1, Menu.NONE, "Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        return if (item.itemId == 1) {
            val lv = findViewById<ListView>(R.id.session_list)
            val obj = lv.getItemAtPosition(info.position) as SessionItem
            //Delete Audio
            val mp3 = File(externalMediaDirs[0].absolutePath + "/" + obj.filename + ".mp3")
            mp3.delete()
            //Delete Metainfo
            val json = File(externalMediaDirs[0].absolutePath + "/" + obj.filename + ".json")
            json.delete()
            //Remove from listview
            sessions?.remove(obj)
            val tmp = lv.adapter as CustomListAdapter
            tmp.notifyDataSetChanged()

            Toast.makeText(this, "${obj.name} deleted", Toast.LENGTH_SHORT).show()
            true
        } else
            super.onContextItemSelected(item)
    }

    private fun getSessionData(): List<SessionItem> {
        val dir = externalMediaDirs[0].absolutePath
        val files = File(dir).listFiles { _, name -> name.endsWith("mp3") }!!
        return files.map(File::nameWithoutExtension).map(::SessionItem)
    }

    private fun checkPermission() {
        val permissions = arrayOf(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE,
            ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        if (permissions.any { perm -> checkSelfPermission(applicationContext, perm) != PERMISSION_GRANTED })
            ActivityCompat.requestPermissions(this, permissions, 0)
    }

    private fun openCallViewActivity(sessionNumber: String) {
        Toast.makeText(this@MainActivity, "Call started", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, ActivityCallView::class.java)
            .putExtra("session", sessionNumber)
        startActivity(intent)
    }

    private fun openMapActivity(sessionName: String) {
        val intent = Intent(this@MainActivity, ActivityMap::class.java)
            .putExtra("session", sessionName )
        startActivity(intent)
    }
}
