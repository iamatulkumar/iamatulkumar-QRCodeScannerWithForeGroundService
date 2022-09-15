package com.androidpro.bookingapp.service

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.androidpro.bookingapp.model.BookingTimerEvent
import com.androidpro.bookingapp.util.Constant

class BookingTimerService: LifecycleService() {

    private val TAG = BookingTimerService::class.java.simpleName

    companion object {
        val bookingTimerEvent = MutableLiveData<BookingTimerEvent>()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constant.ACTION_START_SERVICE -> {
                    Log.i(TAG, "Start booking service")
                    bookingTimerEvent.postValue(BookingTimerEvent.START_SERVICE)
                }
                Constant.ACTION_STOP_SERVICE -> {
                    Log.i(TAG, "End booking service")
                    bookingTimerEvent.postValue(BookingTimerEvent.END_SERVICE)
                }
                else -> {
                    Log.i(TAG, "no action found")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}