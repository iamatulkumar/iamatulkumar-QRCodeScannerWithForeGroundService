package com.androidpro.bookingapp.ui

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.androidpro.bookingapp.BuildConfig
import com.androidpro.bookingapp.R
import com.androidpro.bookingapp.component.BookingAlertDialog
import com.androidpro.bookingapp.databinding.FragmentScannerBinding
import com.androidpro.bookingapp.viewmodel.MainSharedViewmodel
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback

class ScannerFragment : Fragment() {

    private val TAG = ScannerFragment::class.java.simpleName

    private lateinit var codeScanner: CodeScanner
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private val viewModel: MainSharedViewmodel by activityViewModels()

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scannerView = binding.scannerView
        codeScanner = CodeScanner(requireActivity(), scannerView)

        codeScanner.decodeCallback = DecodeCallback { response ->
            activity?.runOnUiThread {
                response.text?.let { onQrCodeScanResponse(it)}
                findNavController().navigate(R.id.action_ScannerFragment_to_BookingFragment)
            }
            response.text?.let { onQrCodeScanResponse(it)}
        }
        codeScanner.errorCallback = ErrorCallback { error: Throwable ->
            activity?.runOnUiThread {
                Toast.makeText(
                    activity,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        registerCameraPermission()
        binding.btnAllowCamera.setOnClickListener{
            callCameraPermission()
        }
    }

    private fun onQrCodeScanResponse(qrCodeResponse: String) = viewModel.scanQRCodeResponse(qrCodeResponse)

    private fun startCodeScanner() {
        codeScanner.startPreview()
        binding.permissionFramelayout.visibility= View.GONE

    }

    private fun callCameraPermission() {
        if (!hasCameraPermission()) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCodeScanner()
        }
    }

    private fun hasCameraPermission(): Boolean {
        val camera = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        )
        return camera == PackageManager.PERMISSION_GRANTED
    }

    private fun registerCameraPermission() {
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    Log.d(TAG, "registerCameraPermission - Camera Permission Granted")
                    startCodeScanner()
                } else {
                    Log.d(TAG, "registerCameraPermission - Camera Permission NOT Granted")
                    requestCameraPermission()
                }
            }
        callCameraPermission()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "requestCameraPermission - Camera Permission Granted")
                startCodeScanner()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Log.d(TAG, "requestCameraPermission - Camera Permission NOT Granted")
                BookingAlertDialog.showDialogOK(
                    message = getString(R.string.permission_dialog_camera_description),
                    context = context
                ) { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        DialogInterface.BUTTON_NEGATIVE -> {dialog.dismiss()}
                    }
                }
            }
            else -> {
                BookingAlertDialog.showDialogOK(
                    message = getString(R.string.permission_dialog_camera_description),
                    context = context
                ){ dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            showSettings()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {dialog.dismiss()}
                    }
                }
            }
        }
    }

    private fun showSettings() {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        }
        startActivity(settingsIntent)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}