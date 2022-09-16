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
import androidx.navigation.fragment.findNavController
import com.androidpro.bookingapp.R
import com.androidpro.bookingapp.component.BookingAlertDialog
import com.androidpro.bookingapp.databinding.FragmentBookingBinding
import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.repository.BookingStatus
import com.androidpro.bookingapp.service.BookingTimerEvent
import com.androidpro.bookingapp.service.BookingTimerService
import com.androidpro.bookingapp.util.Constant
import com.androidpro.bookingapp.util.TimerUtil
import com.androidpro.bookingapp.viewmodel.BookingDetails
import com.androidpro.bookingapp.viewmodel.ClearAllState
import com.androidpro.bookingapp.viewmodel.Failed
import com.androidpro.bookingapp.viewmodel.Loading
import com.androidpro.bookingapp.viewmodel.MainSharedViewmodel
import com.androidpro.bookingapp.viewmodel.ScanQrCodeSuccess

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
        setViewModelObserver()
        setServiceObserver()
    }

    private fun setViewModelObserver() {
        viewModel.fetchBookingDetails()
        viewModel.bookingDetail.observe(viewLifecycleOwner) {
            when(it) {
                is BookingDetails -> {
                    showBookingDetails(it.bookingModel)
                }
                is ScanQrCodeSuccess -> {
                    openConfirmDialog(it.bookingModel)
                }
                is Failed -> {
                    showErrorToast()
                }
                is ClearAllState -> {
                    cleanAllState()
                }
                is Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun setServiceObserver() {
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

    private fun showProgressBar(){
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun cleanAllState() {
        viewModel.clearAllData()
        startBookingTimerService(Constant.ACTION_STOP_SERVICE)
        with(binding) {
            clEmptyState.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            btnScanNow.text = getString(R.string.booking_fragment_scan_now)
        }
    }

    private fun handleScanNow() {
        binding.btnScanNow.setOnClickListener {
            findNavController().navigate(R.id.action_BookingFragment_to_ScannerFragment)
        }
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

    private fun showErrorToast() {
        binding.progressBar.visibility=View.GONE
        Toast.makeText(
            activity,
            "Please scan QR again..!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openConfirmDialog(response: BookingModel) {
        if(response.status != BookingStatus.ACTIVE) {
            BookingAlertDialog.showDialogOK(
                "Complete your booking",
                response.locationDetails,
                context
            ) { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> handleCompleteBooking(response)
                    DialogInterface.BUTTON_NEGATIVE -> {dialog.dismiss()}
                }
            }
        } else {
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
    }

    private fun handleCompleteBooking(response:BookingModel) {
        viewModel.submitBooking(response)
    }

    private fun handleBookingDetails(response: BookingModel) {
        viewModel.saveBookingDetails(response)
        showBookingDetails(response)
    }

    private fun showBookingDetails(response: BookingModel) {
        if(response.status==BookingStatus.ACTIVE) {
            with(binding) {
                clEmptyState.visibility = View.GONE
                qrDetailsView.visibility = View.VISIBLE
                textLocationIdValue.text = response.locationId
                textLocationDetailsValue.text = response.locationDetails
                textPricePerMinuteValue.text = response.pricePerMin
                btnScanNow.text = getString(R.string.booking_fragment_end_bootking_cta)
            }
            if(!isTimerRunning) {startBookingTimerService(Constant.ACTION_START_SERVICE, response.startTime)}
        } else {
            with(binding) {
                clEmptyState.visibility = View.VISIBLE
                qrDetailsView.visibility = View.GONE
                btnScanNow.text = getString(R.string.booking_fragment_scan_now)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}