package com.androidpro.bookingapp.model

import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.repository.BookingStatus
import com.google.gson.annotations.SerializedName

data class QRCodeScanResponse(
    @SerializedName("location_id")
    val locationId: String,
    @SerializedName("location_details")
    val locationDetails: String,
    @SerializedName("price_per_min")
    val pricePerMin: String
) {
    fun toBookingModel(startTime: Long, status:BookingStatus): BookingModel{
        return BookingModel(
            locationId = locationId,
            locationDetails = locationDetails,
            pricePerMin = pricePerMin,
            status = status,
            startTime = startTime
        )
    }
 }
