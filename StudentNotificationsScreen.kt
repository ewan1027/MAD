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
fun StudentNotificationsScreen(studentNotificationsViewModel: StudentNotificationsViewModel = viewModel()) {
    val notificationListState by studentNotificationsViewModel.notificationListState.collectAsState()

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (notificationListState) {
                is NotificationListState.Loading -> {
                    CircularProgressIndicator()
                }
                is NotificationListState.Success -> {
                    val notifications = (notificationListState as NotificationListState.Success).notifications
                    if (notifications.isEmpty()) {
                        Text(
                            text = "No notifications found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationItem(notification = notification)
                            }
                        }
                    }
                }
                is NotificationListState.Error -> {
                    val message = (notificationListState as NotificationListState.Error).message
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
fun NotificationItem(notification: Notification) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
