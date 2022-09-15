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
        val serviceEvent = MutableLiveData<ServiceLiveData>()
    }

    override fun onCreate() {
        super.onCreate()
        initialValue()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constant.ACTION_START_SERVICE -> startForegroundNotificationService()
                Constant.ACTION_STOP_SERVICE -> endForegroundNotificationService()
                else -> {
                    Log.i(TAG, "no action found")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer(){
        val timeStart = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (!isSeviceStopped && serviceEvent.value!! == BookingTimerEvent.START_SERVICE){
                val lapTime = System.currentTimeMillis() - timeStart
                serviceEvent.postValue(TimeInMillis(lapTime))
                delay(50L)
            }
        }
    }

    private fun initialValue() {
        serviceEvent.postValue(BookingTimerEvent.END_SERVICE)
        serviceEvent.postValue(TimeInMillis(0L))
    }

    private fun startForegroundNotificationService() {
        serviceEvent.postValue(BookingTimerEvent.START_SERVICE)
        startTimer()
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        startForeground(
            Constant.NOTIFICATION_ID,
            notificationBuider.build()
        )

        serviceEvent.observe(this) {
            when (it) {
                is TimeInMillis -> {
                    if (!isSeviceStopped) {
                        val notificationBuilder = notificationBuider.setContentText(
                            TimerUtil.getFormattedTime(it.long)
                        )
                        notificationManagerCompat.notify(
                            Constant.NOTIFICATION_ID,
                            notificationBuilder.build()
                        )
                    }
                }
            }

        }
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

interface ServiceLiveData

data class TimeInMillis(val long: Long):ServiceLiveData

sealed class BookingTimerEvent: ServiceLiveData{
    object START_SERVICE:BookingTimerEvent()
    object END_SERVICE:BookingTimerEvent()
}