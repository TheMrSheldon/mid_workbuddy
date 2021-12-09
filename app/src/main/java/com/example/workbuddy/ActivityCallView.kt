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
    private var xmlfile: File? = null
    private var points = mutableListOf<GeoPoint>()

    private var time = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_view)

        val sessionName = intent.getStringExtra("session")

        val cancel = findViewById<MaterialButton>(R.id.end_call_button)
        val mute = findViewById<MaterialButton>(R.id.mute_button)

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
            if (sessionName != null) {
                storeGeoPoints(sessionName)
            }
            openMapActivity(sessionName.toString())
        }

        mute.setOnClickListener {
            toggleAudioInput(mute)
        }

        if (sessionName != null) {
            startAudioInstance(sessionName)

        } else { // TODO reimplement for better error handling
           openMainActivity()
        }
    }

    private fun getCoordinate(l: Location) {
        points.add((GeoPoint(l.latitude, l.longitude)))
        //Log.d("geo", GeoPoint(l.latitude, l.longitude).toString())
    }

    fun openMainActivity() {
        val intent = Intent(this@ActivityCallView, MainActivity::class.java)
        terminateAudioInstance()
        startActivity(intent)
    }

    fun openMapActivity(sessionName: String) {
        Toast.makeText(this@ActivityCallView, "Call closed", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityCallView, ActivityMap::class.java)
        intent.putExtra("session", sessionName + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(
            time
        ))
        terminateAudioInstance()
        startActivity(intent)
    }

    fun startAudioInstance(sessionName: String) {
        // create file
        val dir = externalMediaDirs
        try {
            audiofile = File(externalMediaDirs[0].absolutePath+ "/" + sessionName + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(
                time
            ) + ".mp3")

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


    fun terminateAudioInstance() {
        with(recorder) {
            this?.stop()
            this?.reset()
            this?.release()
        }

    }

    fun toggleAudioInput(button: MaterialButton) {
        // toggle mute
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = !audioManager.isMicrophoneMute

        val newColor = if (audioManager.isMicrophoneMute) Color.RED else Color.DKGRAY
        button.backgroundTintList = ColorStateList.valueOf(newColor)
    }

    private fun storeGeoPoints(name: String) {
        val dir = externalMediaDirs[0].absolutePath+ "/" + name + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(
            time
        ) + ".json"
        val coordinates = File(dir)
        val json = JSONObject()
        Log.d("dir", dir)
        coordinates.bufferedWriter().use { out ->
            for((index, coordinate) in points.withIndex()) {
                json.put("coordinate" + index.toString(), coordinate)
            }
            out.write(json.toString(1))
        }
        //Log.d("json_file", json.toString(1))
    }


}

