package com.nibm.tmapp.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nibm.tmapp.model.Result
import com.nibm.tmapp.model.Submission
import com.nibm.tmapp.model.Assignment // Import Assignment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Import await for suspend functions

sealed class ResultListState {
    object Loading : ResultListState()
    data class Success(val results: List<Result>) : ResultListState()
    data class Error(val message: String) : ResultListState()
}

class ViewResultsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val submissionsCollection = db.collection("submissions")
    private val assignmentsCollection = db.collection("assignments") // New: assignments collection

    private val _resultListState = MutableStateFlow<ResultListState>(ResultListState.Loading)
    val resultListState: StateFlow<ResultListState> = _resultListState

    init {
        fetchResults()
    }

    private fun fetchResults() {
        viewModelScope.launch {
            val studentId = auth.currentUser?.uid
            Log.d("ViewResultsViewModel", "Current studentId: $studentId")
            if (studentId == null) {
                _resultListState.value = ResultListState.Error("User not logged in.")
                return@launch
            }
            try {
                submissionsCollection
                    .whereEqualTo("studentId", studentId)
                    .orderBy("submittedAt", Query.Direction.DESCENDING) // Order by submittedAt
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            _resultListState.value = ResultListState.Error(e.message ?: "Unknown error")
                            return@addSnapshotListener
                        }
                        if (snapshots != null) {
                            val resultsList = mutableListOf<Result>()
                            launch { // Launch a new coroutine for async operations inside listener
                                for (document in snapshots.documents) {
                                    val submission = document.toObject(Submission::class.java)?.copy(documentId = document.id)
                                    if (submission != null && submission.grade != null) {
                                        // Fetch assignment to get the subject name
                                        val assignment = assignmentsCollection.document(submission.assignmentId)
                                            .get().await().toObject(Assignment::class.java)

                                        val result = Result(
                                            id = submission.documentId,
                                            studentId = submission.studentId,
                                            subject = assignment?.title ?: "Unknown Subject", // Use title from Assignment
                                            grade = submission.grade!!, // Grade is guaranteed non-null here
                                            date = submission.submittedAt ?: java.util.Date() // Use submittedAt as date
                                        )
                                        resultsList.add(result)
                                    }
                                }
                                _resultListState.value = ResultListState.Success(resultsList)
                            }
                        } else {
                            _resultListState.value = ResultListState.Success(emptyList())
                        }
                    }
            } catch (e: Exception) {
                _resultListState.value = ResultListState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
