package com.androidpro.bookingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpro.bookingapp.model.QRCodeScanResponse
import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.repository.BookingRepository
import com.androidpro.bookingapp.repository.BookingStatus
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.json.JSONTokener
import javax.inject.Inject

@HiltViewModel
class MainSharedViewmodel @Inject constructor(
    private val bookingRepository: BookingRepository): ViewModel() {

    private val _bookingDetail: MutableLiveData<BookingAction> = MutableLiveData()
    val bookingDetail: LiveData<BookingAction> = _bookingDetail

    fun scanQRCodeResponse(qrResponse: String) {
        viewModelScope.launch {
            try {
                val qrCodeScanResponse:QRCodeScanResponse =
                    Gson().fromJson(JSONTokener(qrResponse).nextValue().toString(), QRCodeScanResponse::class.java)
                _bookingDetail.value = ScanQrCodeSuccess(qrCodeScanResponse.toBookingModel(System.currentTimeMillis(), BookingStatus.ACTIVE))
            }catch (exception:Exception) {
                _bookingDetail.value = Failed("Parsing Failed")
            }
        }
    }

    fun saveBookingDetails(response: BookingModel) {
        viewModelScope.launch {
            bookingRepository.saveBookingData(response)
        }
    }

    fun fetchBookingDetails() {
        viewModelScope.launch {
            bookingRepository.fetchBookingData()
                .flowOn(Dispatchers.IO)
                .collect {
                        value ->
                    _bookingDetail.value = BookingDetails(value)
                }
        }

    }
}

interface BookingAction

data class ScanQrCodeSuccess(val bookingModel: BookingModel):BookingAction
data class BookingDetails(val bookingModel: BookingModel):BookingAction
data class Failed(val message: String):BookingAction
