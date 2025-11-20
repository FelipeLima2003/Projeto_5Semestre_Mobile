package com.example.projetointegrador

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var startTime = 0L
    private var totalDistance = 0.0
    private var lastLocation: Location? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isServiceRunning = false

    companion object {
        const val ACTION_LOCATION_UPDATE = "action_location_update"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_DURATION = "extra_duration"
        const val NOTIFICATION_ID = 123
        const val CHANNEL_ID = "corridas_channel"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura o callback para receber atualizações do GPS
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { currentLocation ->
                    // 1. Calcula distância
                    if (lastLocation != null) {
                        totalDistance += lastLocation!!.distanceTo(currentLocation) // metros
                    }
                    lastLocation = currentLocation

                    // 2. Calcula tempo
                    val durationMillis = SystemClock.elapsedRealtime() - startTime

                    // 3. Envia dados para a Activity
                    sendBroadcast(currentLocation.latitude, currentLocation.longitude, totalDistance, durationMillis)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isServiceRunning) {
            isServiceRunning = true
            startTime = SystemClock.elapsedRealtime()
            startForegroundService()
            startLocationUpdates()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rastreamento de Corrida",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RunConnect")
            .setContentText("Rastreando sua corrida...")
            .setSmallIcon(R.mipmap.ic_launcher) // Certifique-se que este ícone existe
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000) // Atualiza a cada 3 segundos
            .setMinUpdateDistanceMeters(5f) // Ou a cada 5 metros
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun sendBroadcast(lat: Double, lng: Double, dist: Double, time: Long) {
        val intent = Intent(ACTION_LOCATION_UPDATE)
        intent.putExtra(EXTRA_LATITUDE, lat)
        intent.putExtra(EXTRA_LONGITUDE, lng)
        intent.putExtra(EXTRA_DISTANCE, dist)
        intent.putExtra(EXTRA_DURATION, time)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
