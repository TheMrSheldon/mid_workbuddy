package com.example.workbuddy

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat

import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.*
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint

import org.osmdroid.api.IMapController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Polyline.OnClickListener


class ActivityMap : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var map : MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Init Open Street Map
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        //


        setContentView(R.layout.activity_map)
        val button = findViewById<ImageButton>(R.id.return_button)
        button.setOnClickListener {
            openMainActivity()
        }

        map = findViewById(R.id.map)
        val mapController = map.controller
        mapController.setZoom(13.0)
        mapController.setCenter(GeoPoint(52388549.0, 9712665.0))



        val points = ArrayList<GeoPoint>()
        for (i in 0..29){
            points.add(LabelledGeoPoint(
                37 + Math.random() * 5, -8 + Math.random() * 5 , "Point #$i"
            ))
        }
        points.add(points[0])

        drawoverlay(map, mapController, points, listener = Listener(this))
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

    fun drawoverlay(mapView: MapView, controller: IMapController, points: ArrayList<GeoPoint>, listener: OnClickListener){
        // set polyline overlay

        // add overlay
        /*
        val roadManager = OSRMRoadManager(mapView.context, Configuration.getInstance().userAgentValue)
        roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)
        var road = roadManager.getRoad(points)
        val poly = RoadManager.buildRoadOverlay(road)*/
        val poly = Polyline()
        poly.setPoints(points)
        poly.outlinePaint.color = Color.BLUE
        poly.setOnClickListener(listener)
        mapView.overlays.add(poly)

        controller.setCenter(points[0])
    }
    private class Listener(val map: Context) : OnClickListener{
        override fun onClick(polyline: Polyline, mapView: MapView, eventPos: GeoPoint): Boolean {
            for (o in mapView.overlays) {
                if (o is Marker) mapView.overlays.remove(o)
            }
            //Toast.makeText(mapView.context, "polyline with ${polyline.actualPoints.size} pts was tapped", Toast.LENGTH_LONG).show()
            val marker = Marker(mapView)
            marker.position = eventPos

            marker.icon = ContextCompat.getDrawable(this.map, R.drawable.ic_baseline_place_24)

            marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
            mapView.invalidate()
            return false
        }
    }


}