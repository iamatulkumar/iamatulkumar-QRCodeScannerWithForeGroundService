package com.androidpro.bookingapp.model

sealed class BookingTimerEvent{
    object START_SERVICE:BookingTimerEvent()
    object END_SERVICE:BookingTimerEvent()
}