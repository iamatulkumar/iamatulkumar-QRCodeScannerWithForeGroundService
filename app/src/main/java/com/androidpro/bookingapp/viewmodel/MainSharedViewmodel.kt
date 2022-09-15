package com.androidpro.bookingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainSharedViewmodel @Inject constructor(): ViewModel() {

    private val _response: MutableLiveData<String> = MutableLiveData()
    val response: LiveData<String> = _response

    fun scanQRCodeResponse(qrResponse: String) {
        _response.postValue(qrResponse)
    }
}