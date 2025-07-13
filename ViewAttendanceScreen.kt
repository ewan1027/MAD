package com.nibm.tmapp.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ViewAttendanceScreen(viewAttendanceViewModel: ViewAttendanceViewModel = viewModel()) {
    val attendanceListState by viewAttendanceViewModel.attendanceListState.collectAsState()

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (attendanceListState) {
                is AttendanceListState.Loading -> {
                    CircularProgressIndicator()
                }
                is AttendanceListState.Success -> {
                    val records = (attendanceListState as AttendanceListState.Success).records
                    if (records.isEmpty()) {
                        Text(
                            text = "No attendance records found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            items(records) { record ->
                                AttendanceRecordItem(record = record)
                            }
                        }
                    }
                }
                is AttendanceListState.Error -> {
                    val message = (attendanceListState as AttendanceListState.Error).message
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

@Composable
fun AttendanceRecordItem(record: AttendanceRecord) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Present on: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(record.timestamp)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
