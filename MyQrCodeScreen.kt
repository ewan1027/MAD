package com.nibm.tmapp.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MyQrCodeScreen(myQrCodeViewModel: MyQrCodeViewModel = viewModel()) {
    val qrCodeState by myQrCodeViewModel.qrCodeState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = qrCodeState) {
                is QrCodeState.Loading -> {
                    CircularProgressIndicator()
                }
                is QrCodeState.Success -> {
                    val bitmap = state.bitmap
                    Card(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "My QR Code",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Your unique student QR code",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
                is QrCodeState.Error -> {
                    val message = state.message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
