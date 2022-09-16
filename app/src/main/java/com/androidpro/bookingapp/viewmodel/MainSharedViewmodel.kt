package com.androidpro.bookingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpro.bookingapp.model.QRCodeScanResponse
import com.androidpro.bookingapp.model.SubmitBookingRequest
import com.androidpro.bookingapp.repository.BookingApiRepository
import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.repository.BookingRepository
import com.androidpro.bookingapp.repository.BookingStatus
import com.androidpro.bookingapp.util.TimerUtil
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONTokener
import javax.inject.Inject

@HiltViewModel
class MainSharedViewmodel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val bookingApiRepository: BookingApiRepository
    ): ViewModel() {

    private val _bookingDetail: MutableLiveData<Action> = MutableLiveData()
    val bookingDetail: LiveData<Action> = _bookingDetail

    private var isBookingExist: Boolean = false

    fun scanQRCodeResponse(qrResponse: String) {
        viewModelScope.launch {
            try {
                val qrCodeScanResponse:QRCodeScanResponse =
                    Gson().fromJson(JSONTokener(qrResponse).nextValue().toString(), QRCodeScanResponse::class.java)
                if(!isBookingExist) {
                    _bookingDetail.value = ScanQrCodeSuccess(qrCodeScanResponse.toBookingModel(System.currentTimeMillis(), BookingStatus.ACTIVE))
                } else {
                    _bookingDetail.value = ScanQrCodeSuccess(qrCodeScanResponse.toBookingModel(System.currentTimeMillis(), BookingStatus.INACTIVE))
                }
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
                    if(value.status == BookingStatus.ACTIVE) {
                        isBookingExist = true
                    }
                    _bookingDetail.value = BookingDetails(value)
                }
        }
    }

    fun submitBooking(response: BookingModel) = viewModelScope.launch {
        val submitBookingRequest = SubmitBookingRequest(
            location_id = response.locationId,
            time_spent = TimerUtil.getTotalMinutes(response.startTime, System.currentTimeMillis()),
            end_time = System.currentTimeMillis())
       viewModelScope.launch {
           bookingApiRepository.submitBooking(submitBookingRequest)
               .onStart {
                   _bookingDetail.postValue(Loading())
               }
               .flowOn(Dispatchers.IO)
               .catch {
                   _bookingDetail.value = Failed("Response error")
               }
               .collect{
                   isBookingExist = false
                   _bookingDetail.value = ClearAllState()
               }
               }
       }

    fun clearAllData(){
        viewModelScope.launch {
            bookingRepository.clearAll()
        }
    }
}

interface Action

data class ScanQrCodeSuccess(val bookingModel: BookingModel):Action
data class BookingDetails(val bookingModel: BookingModel):Action
data class Failed(val message: String):Action
class ClearAllState():Action
class Loading():Action
