package com.androidpro.bookingapp.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.androidpro.bookingapp.util.Constant.BOOKING_PREFERENCE_LOCATION_ID
import com.androidpro.bookingapp.util.Constant.BOOKING_PREFERENCE_LOCATION_DETAILS
import com.androidpro.bookingapp.util.Constant.BOOKING_PREFERENCE_PRICE_PER_MIN
import com.androidpro.bookingapp.util.Constant.BOOKING_PREFERENCE_VALID
import com.androidpro.bookingapp.util.Constant.BOOKING_PREFERENCE_START_TIME
import com.androidpro.bookingapp.util.getLongData
import com.androidpro.bookingapp.util.getStringData
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ActivityRetainedScoped
class BookingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun saveBookingData(bookingModel: BookingModel) {
        dataStore.edit {
                preferences ->
            preferences[BOOKING_PREFERENCE_LOCATION_ID] = bookingModel.locationId
            preferences[BOOKING_PREFERENCE_LOCATION_DETAILS] = bookingModel.locationDetails
            preferences[BOOKING_PREFERENCE_PRICE_PER_MIN] = bookingModel.pricePerMin
            preferences[BOOKING_PREFERENCE_VALID] = bookingModel.status.name
            preferences[BOOKING_PREFERENCE_START_TIME] = bookingModel.startTime
        }
    }

    suspend fun fetchBookingData(): Flow<BookingModel> {
       return dataStore.data.map {
            BookingModel(
                locationId = BOOKING_PREFERENCE_LOCATION_ID.getStringData(it)?:"",
                locationDetails = BOOKING_PREFERENCE_LOCATION_DETAILS.getStringData(it)?:"",
                pricePerMin = BOOKING_PREFERENCE_PRICE_PER_MIN.getStringData(it)?:"",
                status =  BOOKING_PREFERENCE_VALID.getStringData(it)?.let { BookingStatus.valueOf(it) }?:BookingStatus.BLANK,
                startTime = BOOKING_PREFERENCE_START_TIME.getLongData(it)
            )
        }
    }
}