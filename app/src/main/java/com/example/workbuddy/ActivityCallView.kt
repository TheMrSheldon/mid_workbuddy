package com.example.workbuddy


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Looper
import android.util.Log
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

class ActivityCallView : AppCompatActivity() {

    private var recorder: MediaRecorder? = null
    private var audiofile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var points = arrayOf<GeoPoint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_view)

        val sessionName = intent.getStringExtra("session")

        val cancel = findViewById<MaterialButton>(R.id.end_call_button)
        val mute = findViewById<MaterialButton>(R.id.mute_button)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest  = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 15000
        locationRequest.maxWaitTime = 120000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = OurCallback()
        // define function for buttons
        cancel.setOnClickListener {
            val removeTask = fusedLocationClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //DO STUFF
                } else {
                    //DO STUFF
                }
            }

            openMapActivity()
        }

        mute.setOnClickListener {
            toggleAudioInput()
        }

        if (sessionName != null) {
            startAudioInstance(sessionName)
            startlocationListening(sessionName)

        } else { // TODO reimplement for better error handling
           openMainActivity()
        }
    }

    private fun startlocationListening(sessionName: String) {


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
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
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
    private class OurCallback() : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val lat = (p0.lastLocation.latitude * 1E6)
            val lng = (p0.lastLocation.longitude * 1E6)
            val point = GeoPoint(lat, lng)
            //SAVE POINT
        }
    }

}

