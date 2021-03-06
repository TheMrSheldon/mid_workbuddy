package com.example.workbuddy

import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.masoudss.lib.utils.WaveGravity

import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.*
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.util.GeoPoint

import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.BufferedReader
import java.io.File
import java.lang.Double.max
import java.lang.Double.min
import java.util.stream.IntStream


class ActivityMap : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var player: MediaPlayer
    private lateinit var map : MapView
    private var points = ArrayList<GeoPoint>()
    private var markers = ArrayList<Int>()
    private lateinit var waveformSeekBar: WaveformSeekBar
    lateinit var marker : Marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Init Open Street Map
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        //

        val sessionName = intent.getStringExtra("session")

        setContentView(R.layout.activity_map)


        map = findViewById(R.id.map)
        val mapController = map.controller


        // load json-file and store to points
        val json_file = File(externalMediaDirs[0].absolutePath + "/" + sessionName.toString().split('#')[0] + ".json")
        val json_string = json_file.bufferedReader().use(BufferedReader::readText)
        val json_obj = JSONObject(json_string)
        val keys = json_obj.keys()
        while(keys.hasNext()) {
            val key = keys.next()
            if("Marker" in key) {

                try {
                    val values = json_obj.get(key).toString().split(',')
                    for (v in values){
                        Log.e("FLAGS", v.toInt().toString())
                        markers.add(v.toInt())
                    }
                } catch (e: Exception) {
                    // handler
                    Log.e("FLAGS", "No Flags")
                }

                continue
            }
            val values = json_obj.get(key).toString().split(',')
            points.add((GeoPoint(values[0].toDouble(), values[1].toDouble())))
        }
        //markers = json_obj.getJSONArray("Marker") as ArrayList<GeoPoint>
        // use random sample if no points added
        if (points.isEmpty()) {
            for (i in 0..29) {
                points.add(
                    LabelledGeoPoint(
                        37 + Math.random() * 2, -8 + Math.random() * 2, "Point #$i"
                    )
                )
            }
        points.add(points[0])
        }
        Log.e("TESTING",markers.toString())
        map.addOnFirstLayoutListener { _, _, _, _, _ ->
            val bounds = BoundingBox.fromGeoPoints(points)
            map.zoomToBoundingBox(bounds, false, 5, 20.0, 0)
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
        setflags()
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
                if (fromUser)
                    player.seekTo(progress.toInt())
                updateMarker()
            }
        }

        Thread {
            var last = player.currentPosition
            while (!Thread.currentThread().isInterrupted) {
                if(last == player.currentPosition || !player.isPlaying) continue
                last = player.currentPosition
                waveformSeekBar.progress = player.currentPosition.toFloat()
                //updateMarker()
            }
        }.start()


        val playbutton = findViewById<FloatingActionButton>(R.id.play_button)
        playbutton.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                playbutton.setImageDrawable(resources.getDrawable(R.drawable.ic_media_play))

            }else{
                player.start()
                playbutton.setImageDrawable(resources.getDrawable(R.drawable.ic_media_pause))

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
        if (points.size <= 1)
            return false
        val closest = IntStream.range(0, points.size)
            .mapToObj { i -> Pair(i, projectOntoLine(points[i], points[(i+1) % points.size], eventPos!!)) }
            .filter { pair -> !pair.second.first.isNaN() }
            .min(Comparator.comparingDouble{p-> eventPos!!.distanceToAsDouble(p.second.second)}).get()
        val index = closest.first
        val prevWPTime = index*(player.duration / points.size)
        val nextWPTime = (index+1)*(player.duration / points.size)
        val progressToWP = closest.second.first
        val playbackPos = prevWPTime + progressToWP * (nextWPTime-prevWPTime)
        onReplayPositionSelected(playbackPos, closest.second.second, map)
        return false
    }
    private fun setflags(){
        for (point in markers){
            val flag = Marker(map)
            flag.position = points[point]
            flag.icon = ContextCompat.getDrawable(this, R.drawable.ic_flag)
            flag.icon.setTint(Color.GREEN)
            flag.setAnchor(Marker.ANCHOR_LEFT, Marker.ANCHOR_BOTTOM)
            map.overlays.add(flag)
        }
        map.invalidate()
    }

    private fun onReplayPositionSelected(playbackPos : Double, point : GeoPoint, map: MapView) {
        marker.position = point
        map.invalidate()
        val playing = player.isPlaying
        if(playing) player.pause()
        player.seekTo(playbackPos.toInt())
        if(playing) player.start()
        waveformSeekBar.progress = player.currentPosition.toFloat()
    }

    private fun projectOntoLine(line1 : GeoPoint, line2 : GeoPoint, point : GeoPoint) : Pair<Double, GeoPoint> {
        val aplong = point.longitude - line1.longitude
        val aplat = point.latitude - line1.latitude
        val ablong = line2.longitude - line1.longitude
        val ablat = line2.latitude - line1.latitude

        val ab2 = ablong*ablong + ablat*ablat
        val ap_ab = aplong*ablong + aplat*ablat

        val t = min(max(ap_ab/ab2, 0.0), 1.0)
        return Pair(t, Utils.lerp(line1, line2, t))
    }

    private fun updateMarker() {
        val index = player.currentPosition / (player.duration / points.size)
        val prevWPTime = index*(player.duration / points.size)
        val nextWPTime = (index+1)*(player.duration / points.size)
        val progressToWP = (player.currentPosition-prevWPTime)/(nextWPTime- prevWPTime).toDouble()
        marker.position = Utils.lerp(points[index % points.size], points[(index+1) % points.size], progressToWP)
        map.invalidate()
    }
}

