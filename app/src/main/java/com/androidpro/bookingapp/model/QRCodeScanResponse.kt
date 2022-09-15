package com.androidpro.bookingapp.model

import com.fasterxml.jackson.annotation.JsonProperty

data class QRCodeScanResponse(
    @JsonProperty("location_id")
    val locationId: String,
    @JsonProperty("location_details")
    val locationDetails: String,
    @JsonProperty("price_per_min")
    val pricePerMin: String
)
