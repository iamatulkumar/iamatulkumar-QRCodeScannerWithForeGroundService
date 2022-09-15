package com.androidpro.bookingapp.ui

import android.content.DialogInterface
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
import com.androidpro.bookingapp.viewmodel.MainSharedViewmodel
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.json.JSONTokener

class BookingFragment : Fragment() {

    private val TAG = BookingFragment::class.java.simpleName

    private val viewModel: MainSharedViewmodel by activityViewModels()

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

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
        getQRCodeResponseData()
    }

    private fun handleScanNow() {
        binding.btnScanNow.setOnClickListener {
            findNavController().navigate(R.id.action_BookingFragment_to_ScannerFragment)
        }
    }

    private fun getQRCodeResponseData() {
        viewModel.response.observe(viewLifecycleOwner, Observer { response ->
            when {
               response.isNotEmpty() && response.isNotBlank() ->
                   verifyResponse(response)
            }
        })
    }

    private fun verifyResponse(response: String) {
        try {
            val qrCodeScanResponse:QRCodeScanResponse =
                Gson().fromJson(JSONTokener(response).nextValue().toString(), QRCodeScanResponse::class.java)
            openConfirmDialog(qrCodeScanResponse)
        }catch (exception:Exception) {
            Log.i(TAG, "Error in parsing QR response")
            showErrorToast()
        }
    }

    private fun showErrorToast() {
        Toast.makeText(
            activity,
            "Please scan QR again..!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openConfirmDialog(response: QRCodeScanResponse) {
        BookingAlertDialog.showDialogOK(
            response.locationId,
            response.locationDetails,
            context
        ) { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> showBookingDetails(response)
                DialogInterface.BUTTON_NEGATIVE -> {dialog.dismiss()}
            }
        }
    }

    private fun showBookingDetails(response: QRCodeScanResponse) {
        with(binding) {
            clEmptyState.visibility = View.GONE
            qrDetailsView.visibility = View.VISIBLE
            textLocationIdValue.text = response.locationId
            textLocationDetailsValue.text = response.locationDetails
            textPricePerMinuteValue.text = response.pricePerMin
            btnScanNow.text = "Complete booking"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}