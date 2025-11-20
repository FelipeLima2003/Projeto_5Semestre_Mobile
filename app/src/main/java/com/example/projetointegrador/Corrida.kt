package com.example.projetointegrador

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.util.concurrent.TimeUnit

class CorridaActivity : BaseActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var txtTempo: TextView
    private lateinit var txtDistancia: TextView
    private lateinit var btnParar: Button

    private val pathPoints = mutableListOf<LatLng>()


    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == LocationService.ACTION_LOCATION_UPDATE) {
                val lat = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0)
                val lng = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0)
                val dist = intent.getDoubleExtra(LocationService.EXTRA_DISTANCE, 0.0)
                val duration = intent.getLongExtra(LocationService.EXTRA_DURATION, 0L)

                updateUI(lat, lng, dist, duration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_corrida)

        txtTempo = findViewById(R.id.txt_tempo)
        txtDistancia = findViewById(R.id.txt_distancia)
        btnParar = findViewById(R.id.btn_parar_corrida)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnParar.setOnClickListener {
            pararCorrida()
        }

        iniciarServico()
    }

    private fun iniciarServico() {
        val intent = Intent(this, LocationService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationReceiver,
            IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    private fun updateUI(lat: Double, lng: Double, distanceMeters: Double, durationMillis: Long) {
        val currentLatLng = LatLng(lat, lng)
        pathPoints.add(currentLatLng)

        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        txtTempo.text = String.format("Tempo: %02d:%02d:%02d", hours, minutes, seconds)


        val distanceKm = distanceMeters / 1000.0
        txtDistancia.text = String.format("Distância: %.2f km", distanceKm)


        googleMap?.clear()

        val polylineOptions = PolylineOptions()
            .addAll(pathPoints)
            .color(Color.BLUE)
            .width(10f)
        googleMap?.addPolyline(polylineOptions)

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
    }

    private fun pararCorrida() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
        Toast.makeText(this, "Corrida Finalizada!", Toast.LENGTH_LONG).show()
        finish()
    }
}
