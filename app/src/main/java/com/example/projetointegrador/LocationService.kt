package com.example.projetointegrador

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
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
        val locationData = MutableLiveData<Location?>()
        val distanceData = MutableLiveData<Double>(0.0)
        val durationData = MutableLiveData<Long>(0L)

        const val NOTIFICATION_ID = 123
        const val CHANNEL_ID = "corridas_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService", "onCreate")
        
        // Garante que os valores iniciais estão definidos
        if (distanceData.value == null) distanceData.postValue(0.0)
        if (durationData.value == null) durationData.postValue(0L)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { currentLocation ->
                    locationData.postValue(currentLocation)

                    if (lastLocation != null) {
                        totalDistance += lastLocation!!.distanceTo(currentLocation).toDouble()
                        distanceData.postValue(totalDistance)
                    }
                    lastLocation = currentLocation
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceInternal()

        if (!isServiceRunning) {
            isServiceRunning = true
            startTime = SystemClock.elapsedRealtime()
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
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun startForegroundServiceInternal() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rastreamento de Corrida",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação de acompanhamento de corrida"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, CorridaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RunConnect")
            .setContentText("Rastreando sua corrida...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error starting foreground: ${e.message}")
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateDistanceMeters(5f)
            .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("LocationService", "SecurityException: ${e.message}")
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
