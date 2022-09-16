package com.androidpro.bookingapp.model

data class SubmitBookingRequest(
    val location_id: String,
    val time_spent: Int,
    val end_time: Long)