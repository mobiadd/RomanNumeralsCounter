package ru.agentplus.romannumeralscounter

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils.join
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.Collections.nCopies


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
        val anim = AlphaAnimation(1.0f, 0.2f)
        anim.duration = 500
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                tv_count.text = getRomanNumber(count)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        tv_count.startAnimation(anim)
    }

    fun getRomanNumber(number: Int): String {
        return join("", nCopies<String>(number, "I"))
            .replace("IIIII", "V")
            .replace("IIII", "IV")
            .replace("VV", "X")
            .replace("VIV", "IX")
            .replace("XXXXX", "L")
            .replace("XXXX", "XL")
            .replace("LL", "C")
            .replace("LXL", "XC")
            .replace("CCCCC", "D")
            .replace("CCCC", "CD")
            .replace("DD", "M")
            .replace("DCD", "CM")
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
        unbindService(connection)
        super.onDestroy()
    }

}
