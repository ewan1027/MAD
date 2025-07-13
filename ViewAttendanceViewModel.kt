package com.nibm.tmapp.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class AttendanceRecord(
    val studentId: String = "",
    val timestamp: Date = Date()
)

sealed class AttendanceListState {
    object Loading : AttendanceListState()
    data class Success(val records: List<AttendanceRecord>) : AttendanceListState()
    data class Error(val message: String) : AttendanceListState()
}

class ViewAttendanceViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _attendanceListState = MutableStateFlow<AttendanceListState>(AttendanceListState.Loading)
    val attendanceListState: StateFlow<AttendanceListState> = _attendanceListState

    init {
        fetchAttendance()
    }

    private fun fetchAttendance() {
        viewModelScope.launch {
            val studentId = auth.currentUser?.uid
            if (studentId == null) {
                _attendanceListState.value = AttendanceListState.Error("User not logged in.")
                return@launch
            }

            try {
                db.collection("attendance")
                    .whereEqualTo("studentId", studentId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            _attendanceListState.value = AttendanceListState.Error(e.message ?: "Unknown error")
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {
                            val records = snapshots.toObjects(AttendanceRecord::class.java)
                            _attendanceListState.value = AttendanceListState.Success(records)
                        } else {
                            _attendanceListState.value = AttendanceListState.Success(emptyList())
                        }
                    }
            } catch (e: Exception) {
                _attendanceListState.value = AttendanceListState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
