package com.example.workbuddy

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workbuddy.databinding.ActivityMainBinding
import android.preference.PreferenceManager

import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.*

class ActivityMap : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private lateinit var map : MapView;

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

        map = findViewById<MapView>(R.id.map)
        val mapController = map.getController()
        mapController.setZoom(13)
        mapController.setCenter(GeoPoint(52388549, 9712665))
    }

    fun openMainActivity() {
        Toast.makeText(this@ActivityMap, "You clicked me.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ActivityMap, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause();
        map.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val permissionsToRequest = ArrayList<String>();
        var i = 0;
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i]);
            i++;
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /*class AnOverlay : ItemizedOverlay<OverlayItem>() {
        private val locations = ArrayList<OverlayItem>()
        private var marker : Drawable

        public AnOverlay(marker : Drawable) {
            super(marker)
            this.marker = marker
            locations.add(OverlayItem(gbf, "Hauptbahnhof", "Hauptbahnhof"))
            locations.add(OverlayItem(uni, "Universität", "Universität"))
            populate()
        }
    }*/
}