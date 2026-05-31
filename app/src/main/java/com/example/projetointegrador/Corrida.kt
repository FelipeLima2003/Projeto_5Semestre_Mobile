package com.example.projetointegrador

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private var startTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {

        RetrofitClient.ensureTokenIsLoaded(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_corrida)

        txtTempo = findViewById(R.id.txt_tempo)
        txtDistancia = findViewById(R.id.txt_distancia)
        btnParar = findViewById(R.id.btn_parar_corrida)


        startTimeMillis = System.currentTimeMillis()

        loggedInUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)
        if (loggedInUserId == -1) {
            Log.w("CorridaActivity", "ID do usuário não encontrado no Intent!")
            Toast.makeText(this, "Erro: ID do usuário não encontrado.", Toast.LENGTH_SHORT).show()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnParar.setOnClickListener {
            pararCorrida()
        }

        verificarPermissoesEIniciar()
        observarDadosDoServico()
    }

    private fun verificarPermissoesEIniciar() {
        val permissoes = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissoes.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissoesNegadas = permissoes.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissoesNegadas.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissoesNegadas.toTypedArray(), 100)
        } else {
            iniciarServico()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            val fineLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarseLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (fineLocationGranted || coarseLocationGranted) {
                iniciarServico()
            } else {
                Toast.makeText(this, "Permissão de localização é necessária para rastrear a corrida.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun iniciarServico() {
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun observarDadosDoServico() {
        LocationService.locationData.observe(this) { location ->
            location?.let {
                val newPoint = LatLng(it.latitude, it.longitude)
                pathPoints.add(newPoint)
                desenharTrajetoria()
                moverCamera(newPoint)
            }
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


        RetrofitClient.ensureTokenIsLoaded(this)

        if (loggedInUserId != -1) {
            salvarDadosDaCorrida()
        } else {
            Toast.makeText(this, "Erro: Usuário não identificado para salvar. Faça login novamente.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun salvarDadosDaCorrida() {

        val usuarioIdParaSalvar = this.loggedInUserId


        Log.d("DEBUG_CORRIDA", "Tentando salvar com ID: $usuarioIdParaSalvar")

        if (usuarioIdParaSalvar <= 0) {
            Toast.makeText(this, "Erro crítico: ID do usuário inválido ($usuarioIdParaSalvar)", Toast.LENGTH_LONG).show()
            return
        }


        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dataInicialFormatada = sdf.format(Date(startTimeMillis))
        val dataFinalFormatada = sdf.format(Date())


        val corridaRequest = CorridaRequest(
            usuarioId = usuarioIdParaSalvar,
            distancia = finalDistance / 1000.0,
            tempoInicial = dataInicialFormatada,
            tempoFinal = dataFinalFormatada
        )


        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.salvarCorrida(corridaRequest)
                if (response.isSuccessful) {
                    Toast.makeText(this@CorridaActivity, "Corrida salva com sucesso!", Toast.LENGTH_LONG).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CorridaActivity", "Erro API (${response.code()}): $errorBody")

                    if (response.code() == 403) {
                        Toast.makeText(this@CorridaActivity, "Sessão expirada (403). Faça login novamente.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@CorridaActivity, "Erro ao salvar: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CorridaActivity", "Falha de rede", e)
                Toast.makeText(this@CorridaActivity, "Falha na conexão.", Toast.LENGTH_SHORT).show()
            } finally {
                finish()
            }
        }
    }
}
