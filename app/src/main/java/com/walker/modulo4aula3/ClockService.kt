package com.walker.modulo4aula3


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.Timer
import java.util.TimerTask

const val TIME_KEY = "TIME_KEY"
const val TIME_UPDATED_KEY = "TIME_UPDATED_KEY"

class ClockService: Service() {
    private val timer = Timer()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val time = intent.getDoubleExtra(TIME_KEY, 0.0)
        timer.schedule(Timer(time), 0 , 1000)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    private inner class Timer(private var time: Double) : TimerTask() {
        override fun run() {
            time++
            val intent = Intent(TIME_UPDATED_KEY)
            intent.putExtra(TIME_KEY, time)
            sendBroadcast(intent)
            Log.d(TIME_KEY, time.toString())
        }
    }
}