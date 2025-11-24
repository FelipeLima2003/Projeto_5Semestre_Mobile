package com.example.projetointegrador

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CorridaActivity : BaseActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var txtTempo: TextView
    private lateinit var txtDistancia: TextView
    private lateinit var btnParar: Button

    private val pathPoints = mutableListOf<LatLng>()

    private var loggedInUserId: Int = -1
    private var finalDistance: Double = 0.0
    private var finalDuration: Long = 0L
    private var distanciaKm: Double = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_corrida)

        txtTempo = findViewById(R.id.txt_tempo)
        txtDistancia = findViewById(R.id.txt_distancia)
        btnParar = findViewById(R.id.btn_parar_corrida)
        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnParar.setOnClickListener {
            pararCorrida()
        }

        iniciarServico()
        observarDadosDoServico()
    }

    private fun iniciarServico() {
        val intent = Intent(this, LocationService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }


    private fun observarDadosDoServico() {

        LocationService.locationData.observe(this) { location ->
            val newPoint = LatLng(location.latitude, location.longitude)
            pathPoints.add(newPoint)
            desenharTrajetoria()
            moverCamera(newPoint)
        }

        LocationService.distanceData.observe(this) { distance ->
            finalDistance = distance
            val distanceKm = distance / 1000.0
            txtDistancia.text = String.format("Distância: %.2f km", distanceKm)
        }


        LocationService.durationData.observe(this) { duration ->
            finalDuration = duration
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
            txtTempo.text = String.format("Tempo: %02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    private fun desenharTrajetoria() {
        googleMap?.clear()

        val polylineOptions = PolylineOptions()
            .addAll(pathPoints)
            .color(Color.BLUE)
            .width(10f)
        googleMap?.addPolyline(polylineOptions)
    }

    private fun moverCamera(latLng: LatLng) {

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    private fun pararCorrida() {

        val intent = Intent(this, LocationService::class.java)
        stopService(intent)


        if (loggedInUserId != -1 && finalDistance > 0) {
            salvarDadosDaCorrida()
        } else {
            Toast.makeText(this, "Corrida finalizada, mas não foi salva (sem distância percorrida).", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun salvarDadosDaCorrida() {
        val distanciaKm = finalDistance / 1000.0

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())

        val dataFinal = java.util.Date()
        val dataFinalString = sdf.format(dataFinal)

        val dataInicial = java.util.Date(dataFinal.time - finalDuration)
        val dataInicialString = sdf.format(dataInicial)

        val corridaRequest = CorridaRequest(
            usuarioId = loggedInUserId,
            distancia = distanciaKm,
            tempoInicial = dataInicialString,
            tempoFinal = dataFinalString
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.salvarCorrida(corridaRequest)
                if (response.isSuccessful) {
                    Toast.makeText(this@CorridaActivity, "Corrida salva com sucesso!", Toast.LENGTH_LONG).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CorridaActivity", "Erro ao salvar corrida: ${response.code()} - $errorBody")
                    Toast.makeText(this@CorridaActivity, "Erro: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("CorridaActivity", "Falha de rede ao salvar corrida", e)
                Toast.makeText(this@CorridaActivity, "Falha na conexão ao salvar.", Toast.LENGTH_SHORT).show()
            } finally {
                finish()
            }
        }
    }
}