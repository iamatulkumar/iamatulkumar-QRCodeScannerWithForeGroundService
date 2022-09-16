package com.androidpro.bookingapp.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androidpro.bookingapp.R
import com.androidpro.bookingapp.component.BookingAlertDialog
import com.androidpro.bookingapp.databinding.FragmentBookingBinding
import com.androidpro.bookingapp.model.QRCodeScanResponse
import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.service.BookingTimerEvent
import com.androidpro.bookingapp.service.BookingTimerService
import com.androidpro.bookingapp.util.Constant
import com.androidpro.bookingapp.util.TimerUtil
import com.androidpro.bookingapp.viewmodel.BookingDetails
import com.androidpro.bookingapp.viewmodel.Failed
import com.androidpro.bookingapp.viewmodel.MainSharedViewmodel
import com.androidpro.bookingapp.viewmodel.ScanQrCodeSuccess
import com.google.gson.Gson
import org.json.JSONTokener

class BookingFragment : Fragment() {

    private val TAG = BookingFragment::class.java.simpleName

    private val viewModel: MainSharedViewmodel by activityViewModels()

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private var isTimerRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleScanNow()
//        getQRCodeResponseData()
        setServiceObserver()
    }

    private fun setServiceObserver() {
        viewModel.fetchBookingDetails()
        viewModel.bookingDetail.observe(viewLifecycleOwner) {
            when(it) {
                is BookingDetails -> {
                    if(it.bookingModel.locationId.isNotEmpty() && it.bookingModel.locationId.isNotEmpty()) {
                        with(binding) {
                            clEmptyState.visibility = View.GONE
                            qrDetailsView.visibility = View.VISIBLE
                            textLocationIdValue.text = it.bookingModel.locationId
                            textLocationDetailsValue.text = it.bookingModel.locationDetails
                            textPricePerMinuteValue.text = it.bookingModel.pricePerMin
                            btnScanNow.text = getString(R.string.booking_fragment_end_bootking_cta)
                        }
                        toggle(it.bookingModel.startTime)
                    }
                }
                is ScanQrCodeSuccess -> {
                    openConfirmDialog(it.bookingModel)
                }
                is Failed -> {
                    showErrorToast()
                }
            }

        }

        BookingTimerService.serviceEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is BookingTimerEvent.START_SERVICE -> {
                    isTimerRunning = true
                    Log.i(TAG, "start from service")
                }
                is BookingTimerEvent.END_SERVICE -> {
                    isTimerRunning = false
                    Log.i(TAG, "end from service")
                }
            }
        }

        BookingTimerService.bookingTimeInMillis.observe(viewLifecycleOwner) { event ->
            binding.textBookingDurationValue.text = TimerUtil.getFormattedTime(event)
        }
    }

    private fun handleScanNow() {
        binding.btnScanNow.setOnClickListener {
            findNavController().navigate(R.id.action_BookingFragment_to_ScannerFragment)
        }
    }

    private fun toggle(bookingStartTime: Long?=0L){
        startBookingTimerService(Constant.ACTION_START_SERVICE, bookingStartTime)
    }

    private fun startBookingTimerService(action:String, bookingStartTime:Long?=0L) {
        activity?.let {
            it.startService(Intent(requireContext(), BookingTimerService::class.java)
                .apply {
                    this.action = action
                    this.putExtra("bookingStartTime", bookingStartTime)
                })
        }
    }

//    private fun getQRCodeResponseData() {
//        viewModel.response.observe(viewLifecycleOwner, Observer { response ->
//            when {
//               response.isNotEmpty() && response.isNotBlank() ->
//                   verifyResponse(response)
//            }
//        })
//    }
//
//    private fun verifyResponse(response: String) {
//        try {
//            val qrCodeScanResponse:QRCodeScanResponse =
//                Gson().fromJson(JSONTokener(response).nextValue().toString(), QRCodeScanResponse::class.java)
//            //openConfirmDialog(qrCodeScanResponse)
//        }catch (exception:Exception) {
//            Log.i(TAG, "Error in parsing QR response")
//            showErrorToast()
//        }
//    }

    private fun showErrorToast() {
        Toast.makeText(
            activity,
            "Please scan QR again..!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openConfirmDialog(response: BookingModel) {
        BookingAlertDialog.showDialogOK(
            response.locationId,
            response.locationDetails,
            context
        ) { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> handleBookingDetails(response)
                DialogInterface.BUTTON_NEGATIVE -> {dialog.dismiss()}
            }
        }
    }

    private fun handleBookingDetails(response: BookingModel) {
        viewModel.saveBookingDetails(response)
        showBookingDetails(response)
    }

    private fun showBookingDetails(response: BookingModel) {
        with(binding) {
            clEmptyState.visibility = View.GONE
            qrDetailsView.visibility = View.VISIBLE
            textLocationIdValue.text = response.locationId
            textLocationDetailsValue.text = response.locationDetails
            textPricePerMinuteValue.text = response.pricePerMin
            btnScanNow.text = getString(R.string.booking_fragment_end_bootking_cta)
        }
        toggle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}