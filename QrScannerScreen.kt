package com.nibm.tmapp.student

import android.Manifest
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Arrangement
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val studentAttendanceViewModel: StudentAttendanceViewModel = viewModel()
    val attendanceState by studentAttendanceViewModel.attendanceState.collectAsState()

    if (cameraPermissionState.status.isGranted) {
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val cameraProvider = cameraProviderFuture.get()
        val previewView = remember { PreviewView(context) }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), @androidx.camera.core.ExperimentalGetImage { 
                    imageProxy ->
                    val image = imageProxy.image
                    if (image != null) {
                        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = BarcodeScanning.getClient(options)
                        scanner.process(inputImage)
                            .addOnSuccessListener {
                                barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { qrCodeValue ->
                                        studentAttendanceViewModel.markAttendance(qrCodeValue)
                                    }
                                }
                            }
                            .addOnFailureListener {

                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                })
            }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

            when (attendanceState) {
                is StudentAttendanceState.Idle -> {
                    // Show camera preview
                }
                is StudentAttendanceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StudentAttendanceState.Success -> {
                    val message = (attendanceState as StudentAttendanceState.Success).message
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { studentAttendanceViewModel.resetState() }, shape = RoundedCornerShape(16.dp)) {
                            Text("Scan Again")
                        }
                    }
                }
                is StudentAttendanceState.Error -> {
                    val message = (attendanceState as StudentAttendanceState.Error).message
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { studentAttendanceViewModel.resetState() }, shape = RoundedCornerShape(16.dp)) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }

        LaunchedEffect(cameraProvider) {
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to scan QR codes.")
        }
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}
