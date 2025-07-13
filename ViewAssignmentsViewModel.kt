package com.nibm.tmapp.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.nibm.tmapp.model.Assignment
import com.nibm.tmapp.model.Submission
import java.util.Date

sealed class AssignmentListState {
    object Loading : AssignmentListState()
    data class Success(val assignments: List<Assignment>) : AssignmentListState()
    data class Error(val message: String) : AssignmentListState()
}

class ViewAssignmentsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _assignmentListState = MutableStateFlow<AssignmentListState>(AssignmentListState.Loading)
    val assignmentListState: StateFlow<AssignmentListState> = _assignmentListState

    init {
        fetchAssignments()
    }

    private fun fetchAssignments() {
        viewModelScope.launch {
            try {
                db.collection("assignments")
                    .orderBy("dueDate", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            _assignmentListState.value = AssignmentListState.Error(e.message ?: "Unknown error")
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {
                            val assignments = snapshots.toObjects(Assignment::class.java)
                            _assignmentListState.value = AssignmentListState.Success(assignments)
                        } else {
                            _assignmentListState.value = AssignmentListState.Success(emptyList())
                        }
                    }
            } catch (e: Exception) {
                _assignmentListState.value = AssignmentListState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun getStudentSubmissionForAssignment(assignmentId: String, studentId: String): StateFlow<Submission?> {
        val submissionFlow = MutableStateFlow<Submission?>(null)
        viewModelScope.launch {
            db.collection("submissions")
                .whereEqualTo("assignmentId", assignmentId)
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        // Handle error
                        submissionFlow.value = null
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        submissionFlow.value = snapshots.documents[0].toObject(Submission::class.java)?.copy(documentId = snapshots.documents[0].id)
                    } else {
                        submissionFlow.value = null
                    }
                }
        }
        return submissionFlow
    }
}
