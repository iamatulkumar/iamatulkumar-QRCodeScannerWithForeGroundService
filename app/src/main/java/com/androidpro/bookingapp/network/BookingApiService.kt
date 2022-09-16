package com.androidpro.bookingapp.network

import com.androidpro.bookingapp.model.SubmitBookingRequest
import com.androidpro.bookingapp.model.SubmitBookingResponse
import com.androidpro.bookingapp.util.Constant
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookingApiService {
    @POST(Constant.SUBMIT_BOOKING)
    suspend fun submitBooking(@Body submitBookingRequest: SubmitBookingRequest): Response<SubmitBookingResponse>
}