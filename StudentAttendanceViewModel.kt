package com.nibm.tmapp.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

sealed class StudentAttendanceState {
    object Idle : StudentAttendanceState()
    object Loading : StudentAttendanceState()
    data class Success(val message: String) : StudentAttendanceState()
    data class Error(val message: String) : StudentAttendanceState()
}

class StudentAttendanceViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _attendanceState = MutableStateFlow<StudentAttendanceState>(StudentAttendanceState.Idle)
    val attendanceState: StateFlow<StudentAttendanceState> = _attendanceState

    fun markAttendance(studentId: String) {
        viewModelScope.launch {
            _attendanceState.value = StudentAttendanceState.Loading

            try {
                val attendanceRecord = hashMapOf(
                    "studentId" to studentId,
                    "timestamp" to Date()
                )

                db.collection("attendance")
                    .add(attendanceRecord)
                    .addOnSuccessListener {
                        _attendanceState.value = StudentAttendanceState.Success("Attendance marked for student ID: $studentId")
                    }
                    .addOnFailureListener { e ->
                        _attendanceState.value = StudentAttendanceState.Error("Failed to mark attendance: ${e.message}")
                    }
            } catch (e: Exception) {
                _attendanceState.value = StudentAttendanceState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun resetState() {
        _attendanceState.value = StudentAttendanceState.Idle
    }
}
