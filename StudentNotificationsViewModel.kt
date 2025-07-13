package com.nibm.tmapp.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Notification(
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0
)

sealed class NotificationListState {
    object Loading : NotificationListState()
    data class Success(val notifications: List<Notification>) : NotificationListState()
    data class Error(val message: String) : NotificationListState()
}

class StudentNotificationsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _notificationListState = MutableStateFlow<NotificationListState>(NotificationListState.Loading)
    val notificationListState: StateFlow<NotificationListState> = _notificationListState

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        viewModelScope.launch {
            try {
                db.collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            _notificationListState.value = NotificationListState.Error(e.message ?: "Unknown error")
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {
                            val notifications = snapshots.toObjects(Notification::class.java)
                            _notificationListState.value = NotificationListState.Success(notifications)
                        } else {
                            _notificationListState.value = NotificationListState.Success(emptyList())
                        }
                    }
            } catch (e: Exception) {
                _notificationListState.value = NotificationListState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
