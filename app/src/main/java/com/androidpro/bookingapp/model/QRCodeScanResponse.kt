package com.androidpro.bookingapp.model

import com.google.gson.annotations.SerializedName

data class QRCodeScanResponse(
    @SerializedName("location_id")
    val locationId: String,
    @SerializedName("location_details")
    val locationDetails: String,
    @SerializedName("price_per_min")
    val pricePerMin: String
)
