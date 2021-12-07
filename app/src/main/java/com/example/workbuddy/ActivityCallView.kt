package com.example.workbuddy


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Xml
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ActivityCallView : AppCompatActivity() {

    private var recorder: MediaRecorder? = null
    private var audiofile: File? = null
    private var xmlfile: File? = null
    private var points = mutableListOf<GeoPoint>()

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
                //storeGeoPoints(sessionName)
            }
            openMapActivity()
        }

        mute.setOnClickListener {
            toggleAudioInput()
        }

        if (sessionName != null) {
            startAudioInstance(sessionName)

        } else { // TODO reimplement for better error handling
           openMainActivity()
        }
    }

    private fun getCoordinate(l: Location) {
        points.add(GeoPoint(l.latitude, l.longitude))
    }

    fun openMainActivity() {
        val intent = Intent(this@ActivityCallView, MainActivity::class.java)
        terminateAudioInstance()
        startActivity(intent)
    }

    fun openMapActivity() {
        Toast.makeText(this@ActivityCallView, "Call closed", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityCallView, ActivityMap::class.java)
        terminateAudioInstance()
        startActivity(intent)
    }

    fun startAudioInstance(sessionName: String) {
        // create file
        val dir = externalMediaDirs
        try {
            audiofile = File.createTempFile(sessionName, ".mp3", dir[0])
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

    fun toggleAudioInput() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = !audioManager.isMicrophoneMute

        val toast = Toast.makeText(applicationContext, audioManager.isMicrophoneMute.toString(), Toast.LENGTH_SHORT)
        toast.show()

    }

    private fun storeGeoPoints(name: String) {
        val dir = externalMediaDirs[0].absolutePath + name + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(
            Date()
        ) + ".jsonl"
        val coordinates = File(dir)
        Log.d("dir", dir)
        coordinates.bufferedWriter().use { out ->
            out.write("{\n")
            for((index, coordinate) in points.withIndex()) {
                out.write("    \"coordinate" + index.toString() + "\":" + coordinate.toString() + "\n")
            }
            out.write("}")
        }
    }


}

