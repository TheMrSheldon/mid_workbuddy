package com.example.workbuddy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityCallView : AppCompatActivity() {
    private var recorder: MediaRecorder? = null
    private var audiofile: File? = null
    private var points = mutableListOf<GeoPoint>()
    private var marker = mutableListOf<GeoPoint>()
    private lateinit var mute: MaterialButton
    private lateinit var flag: MaterialButton
    private lateinit var sessionID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_view)

        val activityListView: ListView = findViewById(R.id.member_list);
        val exampleActivities: Array<String> = getResources().getStringArray(R.array.array_example_members);
        val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_list_item_1, exampleActivities)
        activityListView.adapter = arrayAdapter

        val sessionName = intent.getStringExtra("session")
        val time = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        sessionID = "${sessionName}_${time}"


        val cancel = findViewById<MaterialButton>(R.id.end_call_button)
        flag = findViewById<MaterialButton>(R.id.flag_button)
        mute = findViewById<MaterialButton>(R.id.mute_button)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5.0f, this::getCoordinate)

        // define function for buttons
        cancel.setOnClickListener {
            locationManager.removeUpdates(this::getCoordinate)
            storeGeoPoints()
            openMainActivity()
        }
        mute.setOnClickListener { toggleAudioInput() }
        flag.setOnClickListener { getMarkerCoordinate() }
        startAudioInstance()
        startClock()
        updateMuteButton()
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext, "Disabled Back Press", Toast.LENGTH_SHORT).show()
    }

    private fun getCoordinate(l: Location) {
        points.add((GeoPoint(l.latitude, l.longitude)))
    }

    private fun getMarkerCoordinate() {
        if (points.count() > 0) { // what should happen if there is no point at the moment?
            marker.add((points.last()))
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this@ActivityCallView, MainActivity::class.java)
        terminateAudioInstance()
        startActivity(intent)
    }

    private fun startAudioInstance() {
        // create file
        try {
            val filename = "${externalMediaDirs[0].absolutePath}/$sessionID.mp3"
            audiofile = File(filename)
        } catch (e: IOException) {
            Log.e("Audiofile", "storage error");
        }

        // create mediarecorder and start
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audiofile?.absolutePath)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e("MediaRecorderObject", "prepare() failed")
            }
            start()
        }
    }

    private fun terminateAudioInstance() {
        with(recorder) {
            this?.stop()
            this?.reset()
            this?.release()
        }
    }

    fun toggleAudioInput() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = !audioManager.isMicrophoneMute
        updateMuteButton()
    }

    fun updateMuteButton() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        var newColor = Color.DKGRAY
        var toastMsg = "unmute"
        if (audioManager.isMicrophoneMute) {
            newColor = Color.RED
            toastMsg = "mute"
        }
        mute.backgroundTintList = ColorStateList.valueOf(newColor)
        Toast.makeText(this@ActivityCallView, toastMsg, Toast.LENGTH_SHORT).show()
    }

    private fun storeGeoPoints() {
        val coordinates = File("${externalMediaDirs[0].absolutePath}/$sessionID.json")
        val json = JSONObject()
        coordinates.bufferedWriter().use { out ->
            for((index, coordinate) in points.withIndex())
                json.put("coordinate$index", coordinate)
            json.put("Marker", marker)
            out.write(json.toString(1))
        }
    }

    private fun startClock() {
        val t: Thread = object : Thread() {
            override fun run() {
                try {
                    val time_meeting: TextView = findViewById(R.id.time);
                    var count = 0
                    while (!isInterrupted) {
                        sleep(1000)
                        runOnUiThread {
                            count += 1
                            //val c = Calendar.getInstance()
                            val hours = count / 3600
                            val minutes = (count - hours * 3600) / 60
                            val seconds = (count - hours * 3600) - minutes * 60
                            val curTime =
                                String.format("%02d:%02d:%02d", hours, minutes, seconds)
                            time_meeting.setText(curTime) //change clock to your textview
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        t.start()
    }
}

