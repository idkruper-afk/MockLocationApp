package com.example.mocklocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var etLat: TextInputEditText
    private lateinit var etLon: TextInputEditText
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etLat = findViewById(R.id.etLatitude)
        etLon = findViewById(R.id.etLongitude)
        tvStatus = findViewById(R.id.tvStatus)

        requestRequiredPermissions()

        findViewById<Button>(R.id.btnLondon).setOnClickListener {
            etLat.setText("51.5074")
            etLon.setText("-0.1278")
        }

        findViewById<Button>(R.id.btnNewYork).setOnClickListener {
            etLat.setText("40.7128")
            etLon.setText("-74.0060")
        }

        findViewById<Button>(R.id.btnTokyo).setOnClickListener {
            etLat.setText("35.6762")
            etLon.setText("139.6503")
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val lat = etLat.text.toString().toDoubleOrNull()
            val lon = etLon.text.toString().toDoubleOrNull()

            if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                Toast.makeText(this, "Please enter valid GPS coordinates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviceIntent = Intent(this, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_START
                putExtra(MockLocationService.EXTRA_LAT, lat)
                putExtra(MockLocationService.EXTRA_LON, lon)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
            tvStatus.text = "Status: Active ($lat, $lon)"
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            val serviceIntent = Intent(this, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_STOP
            }
            startService(serviceIntent)
            tvStatus.text = "Status: Inactive"
        }

        findViewById<Button>(R.id.btnDevSettings).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "Enable Developer Options in Settings first", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }
}
