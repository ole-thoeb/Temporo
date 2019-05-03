package com.eloem.temporo

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import com.eloem.temporo.util.foregroundServiceNotification

class TimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    var timer: CountDownTimer? = null
    var isActive = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isActive) {
            isActive = true
            val notification = foregroundServiceNotification("Timer", "test")
            val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            timer = object : CountDownTimer(1000 * 30, 1000) {
                override fun onFinish() {
                    this@TimerService.stopSelf()
                }

                override fun onTick(millisUntilFinished: Long) {
                    Log.d("TimerService", "remaining: $millisUntilFinished")
                    notiManager.notify(
                        startId,
                        foregroundServiceNotification("Timer", (millisUntilFinished / 1000).toString())
                    )
                }

            }.start()

            startForeground(startId, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
