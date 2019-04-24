package ru.agentplus.romannumeralscounter

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class CounterService : Service() {
    private var isRunning = false
    private var count = 0
    private val binder = CounterBinder()
    private lateinit var countHandler: Handler
    private lateinit var runnableTask: Runnable
    private lateinit var broadcastManager: LocalBroadcastManager

    override fun onCreate() {
        Log.d(TAG, "Service onCreate")
        broadcastManager = LocalBroadcastManager.getInstance(this)
        countHandler = Handler()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        return START_STICKY
    }

    private fun counter() {
        if (!isRunning) {
            isRunning = true
            countHandler.apply {
                runnableTask = object : Runnable {
                    override fun run() {
                        count++
                        Log.i(TAG, "Count: $count")
                        broadcastManager.sendBroadcast(Intent(ACTION_TICK).putExtra("count", count))

                        if (count == MAX_VALUE) count = 0
                        postDelayed(this, DELAY_MS)
                    }
                }
                postDelayed(runnableTask, DELAY_MS)
            }
        }
    }

    fun pauseCount() {
        countHandler.removeCallbacks(runnableTask)
        isRunning = false
    }

    fun playCount() {
        counter()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Service onTaskRemoved")
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
//        isRunning = false
    }

    override fun onBind(arg0: Intent): IBinder? {
        Log.d(TAG, "Service onBind")
        return binder
    }

    inner class CounterBinder : Binder() {
        internal val service: CounterService
            get() = this@CounterService
    }

    companion object {
        const val TAG = "CounterService"
        const val DELAY_MS = 1000L
        const val MAX_VALUE = 100
        const val ACTION_TICK = "action_tick"
    }

}
