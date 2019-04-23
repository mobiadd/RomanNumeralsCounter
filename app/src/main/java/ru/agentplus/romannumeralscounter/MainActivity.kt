package ru.agentplus.romannumeralscounter

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    lateinit var myService: CounterService
    private var isBound = false
    private var isPaused = true

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showCount(intent!!.getIntExtra("count", 0))
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as CounterService.CounterBinder
            myService = binder.service
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startStopToggle()
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(CounterService.ACTION_TICK))
        startCounterService()
    }

    private fun startCounterService() {
        val intent = Intent(this@MainActivity, CounterService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun startStopToggle() {
        when {
            isPaused -> setUnPause()
            else -> setPause()
        }
    }

    private fun setUnPause() {
        myService.playCount()
        isPaused = false
    }

    private fun setPause() {
        myService.pauseCount()
        isPaused = true
    }

    private fun showCount(count: Int) {
        tv_count.text = count.toString()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
        unbindService(connection)
        super.onDestroy()
    }

}
