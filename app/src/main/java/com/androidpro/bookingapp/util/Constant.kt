package com.androidpro.bookingapp.util

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constant {
    const val ACTION_START_SERVICE = "action_start_service"
    const val ACTION_STOP_SERVICE = "action_stop_service"

    const val NOTIFICATION_CHANNEL_ID = "booking_notification_channel_id"
    const val NOTIFICATION_CHANNEL_NAME = "booking_notification_channel_name"
    const val NOTIFICATION_ID = 999

    const val USER_PREFERENCE_NAME = "booking_user_preference_name"

    val BOOKING_PREFERENCE_LOCATION_ID = stringPreferencesKey("booking_preference_location_id")
    val BOOKING_PREFERENCE_LOCATION_DETAILS = stringPreferencesKey("booking_preference_location_details")
    val BOOKING_PREFERENCE_PRICE_PER_MIN = stringPreferencesKey("booking_preference_price_per_min")
    val BOOKING_PREFERENCE_VALID = stringPreferencesKey("booking_preference_valid")
    val BOOKING_PREFERENCE_START_TIME = longPreferencesKey("booking_preference_start_time")

    const val BASE_URL = "https://en478jh796m7w.x.pipedream.net/"
    const val SUBMIT_BOOKING = "submit-session"
}