package com.example.projetointegrador

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val handler = Handler(Looper.getMainLooper())
    private var isServiceRunning = false
    private var startTime = 0L

    private var totalDistance = 0.0
    private var lastLocation: Location? = null


    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
    private val binder = LocalBinder()

    companion object {

        val locationData = MutableLiveData<Location>()
        val distanceData = MutableLiveData<Double>()
        val durationData = MutableLiveData<Long>()

        const val NOTIFICATION_ID = 123
        const val CHANNEL_ID = "corridas_channel"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { currentLocation ->
                    locationData.postValue(currentLocation)

                    if (lastLocation != null) {
                        totalDistance += lastLocation!!.distanceTo(currentLocation) // em metros
                        distanceData.postValue(totalDistance)
                    }
                    lastLocation = currentLocation
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
            startTimerUpdates()
        }
        return START_STICKY
    }

    private fun startTimerUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                if (isServiceRunning) {
                    val durationMillis = SystemClock.elapsedRealtime() - startTime
                    durationData.postValue(durationMillis)
                    handler.postDelayed(this, 1000) // Re-agenda para o próximo segundo
                }
            }
        })
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
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000) // A cada 3 segundos
            .setMinUpdateDistanceMeters(5f) // Ou a cada 5 metros
            .build()
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacksAndMessages(null)
        distanceData.postValue(0.0)
        durationData.postValue(0L)
    }

    override fun onBind(intent: Intent?): IBinder = binder
}
