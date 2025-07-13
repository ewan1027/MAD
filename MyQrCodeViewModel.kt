package com.nibm.tmapp.student

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class QrCodeState {
    object Loading : QrCodeState()
    data class Success(val bitmap: Bitmap) : QrCodeState()
    data class Error(val message: String) : QrCodeState()
}

class MyQrCodeViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val _qrCodeState = MutableStateFlow<QrCodeState>(QrCodeState.Loading)
    val qrCodeState: StateFlow<QrCodeState> = _qrCodeState

    init {
        generateQrCode()
    }

    private fun generateQrCode() {
        viewModelScope.launch {
            val studentId = auth.currentUser?.uid
            if (studentId == null) {
                _qrCodeState.value = QrCodeState.Error("User not logged in.")
                return@launch
            }

            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(studentId, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                _qrCodeState.value = QrCodeState.Success(bmp)
            } catch (e: Exception) {
                _qrCodeState.value = QrCodeState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
