package com.androidpro.bookingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpro.bookingapp.model.QRCodeScanResponse
import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.repository.BookingRepository
import com.androidpro.bookingapp.repository.BookingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainSharedViewmodel @Inject constructor(
    private val bookingRepository: BookingRepository): ViewModel() {

    private val _response: MutableLiveData<String> = MutableLiveData()
    private val bookingDetail: MutableLiveData<BookingModel> = MutableLiveData()
    private val alreadyBookingFound: MutableLiveData<Boolean> = MutableLiveData()
    val response: LiveData<String> = _response

    fun scanQRCodeResponse(qrResponse: String) {
        _response.postValue(qrResponse)
    }

    fun saveBookingDetails(response: QRCodeScanResponse) {
        viewModelScope.launch {
            bookingRepository.saveBookingData(response.toBookingModel(System.currentTimeMillis(), BookingStatus.ACTIVE))
        }
    }

    fun fetchBookingDetails() {
        viewModelScope.launch {
            bookingRepository.fetchBookingData()
                .flowOn(Dispatchers.IO)
                .onEmpty {
                    alreadyBookingFound.value = false
                }
                .collect {
                        value ->
                    alreadyBookingFound.value = true
                    bookingDetail.value = value
                }
        }

    }
}

sealed class DataStoreResult<T>(
    val data: T?=null
) {
    class Success<T>(data: T) : DataStoreResult<T>(data)
    class Error<T> : DataStoreResult<T>()
}