package com.walker.modulo4aula3

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

const val CHANNEL_ID = "CHANNEL_ID"

class MainActivity : AppCompatActivity() {

    private val NOTIFICATION_ID = 1
    private val NOTIFICATION_PERMISSION_ID = 101

    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private lateinit var textResult: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private var time = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        textResult = findViewById(R.id.timerText)

        startButton.setOnClickListener { startStopTimer() }
        resetButton.setOnClickListener { resetTimer() }

        serviceIntent = Intent(applicationContext, ClockService::class.java)
        registerReceiver(updateTime, IntentFilter(TIME_UPDATED_KEY))
    }

    private val updateTime: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            time = intent.getDoubleExtra(TIME_KEY, 0.0)
            textResult.text = getTimeStringFromDouble(time)
            if (time % 10 == 0.0) {
                showNotification(time.toString())
            }
        }
    }

    private fun showNotification(minutes: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal Tempo",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        val notification = notificationBuilder
            .setContentTitle("App de Tempo")
            .setContentText("Já se passou $minutes segundos desde o início da contagem")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(this)) {
            if (checkPermission(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_ID)) {
                notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this@MainActivity, permission) ==
            PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Compreendido. Você não receberá notificações deste aplicativo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTimeStringFromDouble(time: Double): CharSequence? {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Int, minutes: Int, seconds: Int): CharSequence? {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun startStopTimer() {
        if (timerStarted) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        startButton.text = "INICIAR"
        timerStarted = false
    }

    private fun startTimer() {
        serviceIntent.putExtra(TIME_KEY, time)
        startService(serviceIntent)
        startButton.text = "PARAR"
        timerStarted = true
    }

    private fun resetTimer() {
        stopTimer()
        time = 0.0
        textResult.text = getTimeStringFromDouble(time)
    }
}