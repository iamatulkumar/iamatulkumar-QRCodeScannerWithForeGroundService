package com.androidpro.bookingapp.repository

import com.androidpro.bookingapp.model.SubmitBookingRequest
import com.androidpro.bookingapp.network.BookingApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class BookingApiRepository @Inject constructor(
    private val bookingApiService: BookingApiService
){
    suspend fun submitBooking(submitBookingRequest: SubmitBookingRequest) = flow {
            val fooList = bookingApiService.submitBooking(submitBookingRequest)
            emit(fooList)
        }.flowOn(Dispatchers.IO)
}