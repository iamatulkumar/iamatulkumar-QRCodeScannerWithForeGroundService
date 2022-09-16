package com.androidpro.bookingapp.service

import android.app.NotificationChannel
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.androidpro.bookingapp.util.Constant
import com.androidpro.bookingapp.util.TimerUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookingTimerService: LifecycleService() {

    private val TAG = BookingTimerService::class.java.simpleName

    @Inject
    lateinit var notificationManagerCompat: NotificationManagerCompat

    @Inject
    lateinit var notificationBuider: NotificationCompat.Builder

    private var isSeviceStopped = false

    companion object {
        val serviceEvent = MutableLiveData<BookingTimerEvent>()
        val bookingTimeInMillis = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()
        initialValue()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constant.ACTION_START_SERVICE -> startForegroundNotificationService(it.getLongExtra("bookingStartTime", 0L))
                Constant.ACTION_STOP_SERVICE -> endForegroundNotificationService()
                else -> {
                    Log.i(TAG, "no action found")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer(startTime: Long) {
        val timeStart = getStartTime(startTime)
        CoroutineScope(Dispatchers.Main).launch {
            while (!isSeviceStopped && serviceEvent.value!! == BookingTimerEvent.START_SERVICE){
                val lapTime = System.currentTimeMillis() - timeStart
                bookingTimeInMillis.postValue(lapTime)
                delay(500L)
            }
        }
    }

    private fun getStartTime(startTime:Long) = if(startTime == 0L) { System.currentTimeMillis()} else startTime

    private fun initialValue() {
        serviceEvent.postValue(BookingTimerEvent.END_SERVICE)
        bookingTimeInMillis.postValue(0L)
    }

    private fun startForegroundNotificationService(longExtra: Long) {
        serviceEvent.postValue(BookingTimerEvent.START_SERVICE)
        startTimer(longExtra)
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        startForeground(
            Constant.NOTIFICATION_ID,
            notificationBuider.build()
        )

        bookingTimeInMillis.observe(this, Observer {
            if(!isSeviceStopped) {
                val notificationBuilder = notificationBuider.setContentText(
                    TimerUtil.getFormattedTime(it)
                )
                notificationManagerCompat.notify(Constant.NOTIFICATION_ID, notificationBuilder.build())
            }
        })
    }

    private fun endForegroundNotificationService() {
        isSeviceStopped = true
        initialValue()
        notificationManagerCompat.cancel(Constant.NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constant.NOTIFICATION_CHANNEL_ID,
            Constant.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManagerCompat.createNotificationChannel(channel)
    }
}

sealed class BookingTimerEvent{
    object START_SERVICE:BookingTimerEvent()
    object END_SERVICE:BookingTimerEvent()
}