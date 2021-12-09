package com.example.workbuddy

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.masoudss.lib.utils.Utils
import com.masoudss.lib.utils.WaveGravity

import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.*
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint

import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.lang.Double.max
import java.lang.Double.min
import java.util.stream.IntStream


class ActivityMap : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var player: MediaPlayer
    private lateinit var map : MapView
    private var points = ArrayList<GeoPoint>()
    private lateinit var waveformSeekBar: WaveformSeekBar
    lateinit var marker : Marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Init Open Street Map
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        //

        val sessionName = intent.getStringExtra("session")

        setContentView(R.layout.activity_map)
        val button = findViewById<ImageButton>(R.id.return_button)
        button.setOnClickListener {
            openMainActivity()
        }

        map = findViewById(R.id.map)
        val mapController = map.controller

        for (i in 0..29){
            points.add(LabelledGeoPoint(
                37 + Math.random() * 2, -8 + Math.random() * 2 , "Point #$i"
            ))
        }
        points.add(points[0])


        //TODO: MARKOS MACHT DAS HIER MIT DEN PUNKTEN und speichert es im points array ab
        map.addOnFirstLayoutListener { _, _, _, _, _ ->
            val bounds = BoundingBox.fromGeoPoints(points)
            map.zoomToBoundingBox(bounds, false)
            map.invalidate()
        }

        drawOverlay(map, mapController, points)
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                onClickOnMap(map, p);
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                TODO("Not yet implemented")
            }
        }))
        Log.e("TESTING",sessionName.toString())
        marker = Marker(map)
        marker.position = points[0]
        marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_place_24)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)
        map.invalidate()

        val dir = externalMediaDirs
        player = MediaPlayer.create(this, Uri.fromFile(File(dir[0].absolutePath +"/"+ sessionName.toString() + ".mp3")))
        waveformSeekBar = findViewById<WaveformSeekBar>(R.id.waveformSeekBar)
        waveformSeekBar.setSampleFrom(dir[0].absolutePath + "/" + sessionName.toString() + ".mp3")
        waveformSeekBar.progress = 0.0F
        waveformSeekBar.maxProgress = player.duration.toFloat()
        waveformSeekBar.waveBackgroundColor = ContextCompat.getColor(this, R.color.purple_200)
        waveformSeekBar.waveProgressColor = ContextCompat.getColor(this, R.color.purple_700)
        waveformSeekBar.waveGravity = WaveGravity.BOTTOM
        waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {

            override fun onProgressChanged(
                waveformSeekBar: WaveformSeekBar,
                progress: Float,
                fromUser: Boolean
            ) {
                if(fromUser) player.seekTo(progress.toInt())

            }
        }
        var observer = MediaObserver(waveformSeekBar,player, map, points)
        Thread(observer).start()


        val playbutton = findViewById<MaterialButton>(R.id.play_button)
        playbutton.setOnClickListener {
            if(player?.isPlaying){
                player?.pause()
            }else{
                player?.start()
            }

        }
    }

    private fun openMainActivity() {
        val intent = Intent(this@ActivityMap, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val permissionsToRequest = ArrayList<String>()
        permissionsToRequest.addAll(permissions)

        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun drawOverlay(mapView: MapView, controller: IMapController, points: ArrayList<GeoPoint>) {
        val poly = Polyline()
        poly.setPoints(points)
        poly.outlinePaint.color = Color.BLUE
        mapView.overlays.add(poly)

        controller.setCenter(points[0])
    }

    private fun onClickOnMap(mapView: MapView, eventPos: GeoPoint?) : Boolean {
        //mapView.overlays.removeIf { o -> o is Marker }
        val closest = IntStream.range(0, points.size)
            .mapToObj { i -> projectOntoLine(points[i], points[(i+1) % points.size], eventPos!!) }
            .filter { pair -> !pair.first.isNaN() }
            .min(Comparator.comparingDouble{p-> eventPos!!.distanceToAsDouble(p.second)}).get()
        onReplayPositionSelected(closest.first, closest.second, map)
        return false
    }

    private fun onReplayPositionSelected(playbackPos : Double, point : GeoPoint, map: MapView) {
            // Update the marker
        marker.position = point
        map.invalidate()
        val playing = player.isPlaying
        if(playing) player.pause()
        player.seekTo(points.indexOf(point) * (player.duration / points.size))
        if(playing) player.start()
    }



    private fun projectOntoLine(line1 : GeoPoint, line2 : GeoPoint, point : GeoPoint) : Pair<Double, GeoPoint> {
        val aplong = point.longitude - line1.longitude
        val aplat = point.latitude - line1.latitude
        val ablong = line2.longitude - line1.longitude
        val ablat = line2.latitude - line1.latitude

        val ab2 = ablong*ablong + ablat*ablat
        val ap_ab = aplong*ablong + aplat*ablat

        val t = min(max(ap_ab/ab2, 0.0), 1.0)

        val closest = GeoPoint(line1.latitude+ablat*t, line1.longitude + ablong * t)
        return Pair(t, closest)
    }

    private class MediaObserver(
        waveformSeekBar: WaveformSeekBar,
        player: MediaPlayer,
        map: MapView,
        points: ArrayList<GeoPoint>
    ) : Runnable {
        val waveformSeekBar: WaveformSeekBar
        val player: MediaPlayer
        val map: MapView
        val points: ArrayList<GeoPoint>

        init{
            this.waveformSeekBar= waveformSeekBar
            this.player = player
            this.map = map
            this.points = points

        }
        override fun run() {
            var last = player.currentPosition
            while (true){
                if(last == player.currentPosition || !player.isPlaying) continue
                last = player.currentPosition
                waveformSeekBar.progress = player.currentPosition.toFloat()
                //Log.e("Progressbar", player.currentPosition.toString());
                val marker = map.overlays.find { o -> o is Marker } as Marker
                marker.position = points[(player.currentPosition / (player.duration /points.size))] // hier ist n problem
                map.invalidate()
                Thread.sleep(10)


            }
        }
    }



}