package com.androidpro.bookingapp.repository

data class BookingModel(
    val locationId: String,
    val locationDetails: String,
    val pricePerMin: String,
    val status: BookingStatus,
    val startTime: Long
)

enum class BookingStatus {
    ACTIVE, INACTIVE, BLANK
}
