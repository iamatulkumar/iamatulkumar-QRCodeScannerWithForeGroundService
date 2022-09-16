package com.androidpro.bookingapp.util

import androidx.datastore.preferences.core.Preferences

fun Preferences.Key<String>.getStringData(
    preferences: Preferences): String? {
    return preferences[this]
}

fun Preferences.Key<Long>.getLongData(
    preferences: Preferences): Long {
    return preferences[this] ?:0L
}