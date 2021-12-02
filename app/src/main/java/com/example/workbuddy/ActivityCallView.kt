package com.example.workbuddy

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.IOException
import java.nio.file.Path

class ActivityCallView : AppCompatActivity() {

    private var recorder: MediaRecorder? = null
    private var audiofile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_view)

        val sessionName = intent.getStringExtra("session")

        val cancel = findViewById<MaterialButton>(R.id.end_call_button)
        val mute = findViewById<MaterialButton>(R.id.mute_button)

        // define function for buttons
        cancel.setOnClickListener {
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
        val dir = getExternalMediaDirs()
        try {
            audiofile = File.createTempFile(sessionName, ".3gp", dir[0])
        } catch (e: IOException) {
            Log.e("Audiofile", "storage error");
        }

        // create mediarecorder and start
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audiofile?.getAbsolutePath())
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
}