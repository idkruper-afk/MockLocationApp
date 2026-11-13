package com.example.mocklocation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat

class MockLocationService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_LAT = "EXTRA_LAT"
        const val EXTRA_LON = "EXTRA_LON"
        private const val CHANNEL_ID = "MockLocationChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var locationManager: LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var targetLat = 0.0
    private var targetLon = 0.0

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                pushLocation(LocationManager.GPS_PROVIDER)
                pushLocation(LocationManager.NETWORK_PROVIDER)
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                targetLat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
                targetLon = intent.getDoubleExtra(EXTRA_LON, 0.0)
                startForeground(NOTIFICATION_ID, buildNotification("Spoofing: $targetLat, $targetLon"))
                startMocking()
            }
            ACTION_STOP -> {
                stopMocking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startMocking() {
        try {
            setupProvider(LocationManager.GPS_PROVIDER)
            setupProvider(LocationManager.NETWORK_PROVIDER)
            isRunning = true
            handler.removeCallbacks(updateRunnable)
            handler.post(updateRunnable)
            Toast.makeText(this, "Mock location active", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Select this app under 'Select mock location app' in Developer Options!",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
        }
    }

    private fun setupProvider(provider: String) {
        try {
            locationManager.removeTestProvider(provider)
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationManager.addTestProvider(
                provider,
                false, false, false, false, true, true, true,
                ProviderProperties.POWER_USAGE_LOW,
                ProviderProperties.ACCURACY_FINE
            )
        } else {
            @Suppress("DEPRECATION")
            locationManager.addTestProvider(
                provider,
                false, false, false, false, true, true, true,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE
            )
        }
        locationManager.setTestProviderEnabled(provider, true)
    }

    private fun pushLocation(provider: String) {
        val mockLocation = Location(provider).apply {
            latitude = targetLat
            longitude = targetLon
            altitude = 35.0
            accuracy = 3.0f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                verticalAccuracyMeters = 3.0f
                speedAccuracyMetersPerSecond = 0.1f
                bearingAccuracyDegrees = 0.1f
            }
        }
        try {
            locationManager.setTestProviderLocation(provider, mockLocation)
        } catch (_: Exception) {}
    }

    private fun stopMocking() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
            try {
                locationManager.removeTestProvider(provider)
            } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mock Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mock Location Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopMocking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
